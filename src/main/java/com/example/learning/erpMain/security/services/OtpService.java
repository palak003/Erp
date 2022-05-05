package com.example.learning.erpMain.security.services;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService{

    private static final Integer EXPIRE_MINS = 5;

    com.google.common.cache.LoadingCache<String, Integer> otpCache;

    public OtpService(){
        super();
        otpCache = (com.google.common.cache.LoadingCache<String, Integer>) CacheBuilder.newBuilder().
                expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }


    public int getOtp(String name) {

        try{
            return otpCache.get(name);
        }catch (Exception e){
            return 0;
        }
    }



    public void clearOTP(String name) {
        otpCache.invalidate(name);
    }


    public int generateOTP(String mailAddress) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        otpCache.put(mailAddress.toLowerCase(), otp);
        return otp;
    }

}