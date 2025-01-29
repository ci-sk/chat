<template>
  <div>
    <h1>Login</h1>
    <form @submit.prevent="login">
      <input v-model="username" placeholder="Username" required />
      <input v-model="password" type="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>
  </div>
</template>

<script>
import {login} from "../net/index.js";

export default {
  data() {
    return {
      username: '',
      password: '',
    };
  },
  methods: {
    async login() {
      try {
        // const response = await fetch('http://localhost:8080/api/auth/login', {
        //   method: 'POST',
        //   headers: { 'Content-Type': 'application/json' },
        //   body: JSON.stringify({
        //     username: this.username,
        //     password: this.password,
        //   }),
        // });

      //   if (response.ok) {
      //     console.log("login success", response)
      //     const data = await response.json();
      //     localStorage.setItem('token', data.token); // 保存 Token
      //     this.$router.push('/chat'); // 跳转到聊天页面
      //   } else {
      //     alert('Login failed');
      //   }
        login(this.username, this.password, true, (res) => {
          console.log("login success", res)
          localStorage.setItem('token', res.token);
          this.$router.push('/chat');
        })
      } catch (error) {
        console.error('Login error:', error);
      }
    },
  },
};
</script>