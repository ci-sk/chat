package org.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String SECRET_KEY;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    /**
     * 生成 Token
     *
     * @param details 用户详细信息。
     * @param id 用户 ID。
     * @param username 用户名。
     * @return 创建的 JWT 字符串。
     */
    public String createJwt(UserDetails details,int id,String username){
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        Date expire = this.expireTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id",id)
                .withClaim("name",username)
                .withClaim("authorities",details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withIssuedAt(new Date())
                .withExpiresAt(expire)
                .sign(algorithm);
    }

    public boolean isInvalidToken(String uuid){
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }

    public boolean invalidateJwt(String handleToken){
        String token = convertToken(handleToken);
        if(token == null) return false;
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        DecodedJWT jwt = jwtVerifier.verify(token);
        String id = jwt.getId();
        return deleteToken(id,jwt.getExpiresAt());
    }

    private boolean deleteToken(String uuid,Date time){
        if(this.isInvalidToken(uuid))
            return false;
        Date now = new Date();
        long diff = time.getTime() - now.getTime();
        long expire = Math.max(time.getTime()-now.getTime(),0);
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid,"",expire, TimeUnit.MICROSECONDS);
        return true;
    }

    public DecodedJWT resolveJwt(String headerToken){
        String token = convertToken(headerToken);
        if(token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try{
            DecodedJWT verify = jwtVerifier.verify(token);
            if(this.isInvalidToken(verify.getId()))
                return  null;
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt)? null : verify;
        }catch (JWTVerificationException e){
            return null;
        }
    }

    public Date expireTime(){
        //日历
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR,expire *24);
        return  calendar.getTime();
    }

    public UserDetails toUser(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public Integer toId(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }

    private String convertToken(String headerToken){
        if(headerToken == null ||!headerToken.startsWith("Bearer "))
            return null;
        return headerToken.substring(7);
    }

    public Integer getUid(String token){
        DecodedJWT jwt1 = JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token);
        return jwt1.getClaim("id").asInt();
    }

}