package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.dto.Account;
import org.example.entity.vo.request.UserRegisterDTO;
import org.example.mapper.AccountMapper;
import org.example.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    @Autowired
    private AccountMapper mapper;

    private final PasswordEncoder passwordEncoder;

    // 直接注入 PasswordEncoder
    public AccountServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void register(UserRegisterDTO dto) {
        // 检查用户名是否已存在
        if (mapper.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        // 密码加密存储
        Account user = new Account();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 加密关键代码
        mapper.insert(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if(account == null)
        {
            System.out.println("用户名不存在");
            throw new UsernameNotFoundException("用户名或者密码错误");
        }
        return User
                .withUsername(username)
                .password(account.getPassword())
                .build();
    }

    public Account findAccountByNameOrEmail(String text) {
        return mapper.findAccountByNameOrEmail(text);
    }
}
