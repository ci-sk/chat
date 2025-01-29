package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.example.entity.BaseData;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@TableName("account")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Account  implements BaseData {
    @TableId(type = IdType.AUTO)
    private Integer uid;
    private String username;
    private String password;
    private String email;
    //    头像
    private String avatar_url;
    private LocalDateTime create_time;
    private LocalDateTime  last_login;
    private AccountStatus status ;

    public enum AccountStatus {
        online, offline
    }
}
