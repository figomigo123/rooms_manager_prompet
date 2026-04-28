package com.roomsmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.roomsmanager.entity.Usage;
import com.roomsmanager.repository.UsageRepository;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UsageService {

    @Autowired
    private UsageRepository usageRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Start tracking user session
     */
    public void startUserSession(String roomId, String userId) {
        String sessionKey = "session:start:" + roomId + ":" + userId;
        redisTemplate.opsForValue().set(sessionKey, System.currentTimeMillis());
        log.debug("Started tracking session for user: {} in room: {}", userId, roomId);
    }

    /**
     * Record user minutes in a room
     */
    public void recordUserMinutes(String roomId, String userId, double minutes) {
        try {
            // Get user app from room data (simplified - you may need to fetch from room service)
            String appId = getAppIdFromRoom(roomId);

            // Update Redis stats
            updateRedisStats(roomId, userId, appId, minutes);

            // Store in MongoDB for persistence
            Usage usage = new Usage();
            usage.setAppId(appId);
            usage.setRoomId(roomId);
            usage.setUserId(userId);
            usage.setMinutes(minutes);
            usage.setDate(LocalDate.now());

            usageRepository.save(usage);

            log.info("Recorded {} minutes for user {} in room {}", minutes, userId, roomId);
        } catch (Exception e) {
            log.error("Error recording user minutes", e);
        }
    }

    /**
     * Get user minutes for a room
     */
    public double getUserMinutesInRoom(String roomId, String userId) {
        try {
            String key = "user-minutes:" + roomId + ":" + userId;
            Object minutes = redisTemplate.opsForValue().get(key);
            return minutes != null ? Double.parseDouble(minutes.toString()) : 0.0;
        } catch (Exception e) {
            log.error("Error getting user minutes", e);
            return 0.0;
        }
    }

    /**
     * Get total room minutes
     */
    public double getRoomTotalMinutes(String roomId) {
        try {
            String key = "room-total-minutes:" + roomId;
            Object minutes = redisTemplate.opsForValue().get(key);
            return minutes != null ? Double.parseDouble(minutes.toString()) : 0.0;
        } catch (Exception e) {
            log.error("Error getting room total minutes", e);
            return 0.0;
        }
    }

    /**
     * Get app total minutes for today
     */
    public double getAppTodayMinutes(String appId) {
        try {
            String key = "app-today-minutes:" + appId + ":" + LocalDate.now();
            Object minutes = redisTemplate.opsForValue().get(key);
            return minutes != null ? Double.parseDouble(minutes.toString()) : 0.0;
        } catch (Exception e) {
            log.error("Error getting app today minutes", e);
            return 0.0;
        }
    }

    /**
     * Get app total minutes for month
     */
    public double getAppMonthMinutes(String appId, int year, int month) {
        try {
            String key = "app-month-minutes:" + appId + ":" + year + "-" + month;
            Object minutes = redisTemplate.opsForValue().get(key);
            return minutes != null ? Double.parseDouble(minutes.toString()) : 0.0;
        } catch (Exception e) {
            log.error("Error getting app month minutes", e);
            return 0.0;
        }
    }

    /**
     * Get billing info for app
     */
    public Map<String, Object> getAppBillingInfo(String appId) {
        Map<String, Object> billingInfo = new HashMap<>();

        LocalDate today = LocalDate.now();
        double todayMinutes = getAppTodayMinutes(appId);
        double monthMinutes = getAppMonthMinutes(appId, today.getYear(), today.getMonthValue());

        // Calculate costs (assuming $0.004 per minute)
        double costPerMinute = 0.004;
        double todayCost = todayMinutes * costPerMinute;
        double monthCost = monthMinutes * costPerMinute;

        billingInfo.put("appId", appId);
        billingInfo.put("todayMinutes", todayMinutes);
        billingInfo.put("todayCost", todayCost);
        billingInfo.put("monthMinutes", monthMinutes);
        billingInfo.put("monthCost", monthCost);
        billingInfo.put("costPerMinute", costPerMinute);
        billingInfo.put("date", today);

        return billingInfo;
    }

    private void updateRedisStats(String roomId, String userId, String appId, double minutes) {
        LocalDate today = LocalDate.now();

        // User minutes in room
        String userRoomKey = "user-minutes:" + roomId + ":" + userId;
        Double currentUserMinutes = (Double) redisTemplate.opsForValue().get(userRoomKey);
        double newUserMinutes = (currentUserMinutes != null ? currentUserMinutes : 0) + minutes;
        redisTemplate.opsForValue().set(userRoomKey, newUserMinutes);

        // Room total minutes
        String roomKey = "room-total-minutes:" + roomId;
        Double currentRoomMinutes = (Double) redisTemplate.opsForValue().get(roomKey);
        double newRoomMinutes = (currentRoomMinutes != null ? currentRoomMinutes : 0) + minutes;
        redisTemplate.opsForValue().set(roomKey, newRoomMinutes);

        // App today minutes
        String appTodayKey = "app-today-minutes:" + appId + ":" + today;
        Double currentAppTodayMinutes = (Double) redisTemplate.opsForValue().get(appTodayKey);
        double newAppTodayMinutes = (currentAppTodayMinutes != null ? currentAppTodayMinutes : 0) + minutes;
        redisTemplate.opsForValue().set(appTodayKey, newAppTodayMinutes);

        // App month minutes
        String appMonthKey = "app-month-minutes:" + appId + ":" + today.getYear() + "-" + today.getMonthValue();
        Double currentAppMonthMinutes = (Double) redisTemplate.opsForValue().get(appMonthKey);
        double newAppMonthMinutes = (currentAppMonthMinutes != null ? currentAppMonthMinutes : 0) + minutes;
        redisTemplate.opsForValue().set(appMonthKey, newAppMonthMinutes);
    }

    private String getAppIdFromRoom(String roomId) {
        // This would typically fetch from room service
        // For now, return from cache
        String appIdKey = "room-app:" + roomId;
        Object appId = redisTemplate.opsForValue().get(appIdKey);
        return appId != null ? appId.toString() : "unknown";
    }
}
