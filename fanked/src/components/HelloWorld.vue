<script setup>
import { ref } from 'vue'
import {login} from "../net/index.js";

defineProps({
  msg: String,
})
const token = ref()

const tologin = () => {
  const username = document.getElementById('username').value
  const password = document.getElementById('password').value
  // const socket = new WebSocket('ws://localhost:8088')
  login(username, password,false,(res)=>{
    // 登录成功后，存储令牌
    console.log("登录成功");
    // 调用成功回调函数
    let token = res.token; // 保存 Token
    console.log("token", token)
    // 显示聊天界面
    document.getElementById('chat').style.display = 'block';
    // 初始化 WebSocket 连接
    initWebSocket(token);
  })
}
// 初始化 WebSocket 连接
function initWebSocket(token) {
  let socket = new WebSocket(`ws://localhost:8088/ws?token=${token}`);
  socket.onopen = () => {
    console.log('WebSocket connected');
  };
  socket.onmessage = (event) => {
    // 接收消息并显示
    const message = event.data;
    const li = document.createElement('li');
    li.textContent = message;
    console.log("message", message)
    document.getElementById('messages').appendChild(li);
  };
  socket.onclose = () => {
    console.log('WebSocket disconnected');
  };
  socket.onerror = (error) => {
    console.error('WebSocket error:', error);
  };
}

const tosend = () => {
  let socket = new WebSocket(`ws://localhost:8088/ws?token=${token}`);
  const message = document.getElementById('message').value;
  const targetUserId = 2; // 假设目标用户ID为2

  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(JSON.stringify({
      type: 'private',
      targetUserId: targetUserId,
      content: message
    }));
    document.getElementById('message').value = ''; // 清空输入框
  } else {
    alert('WebSocket is not connected');
  }
}


</script>

<template>
  <div>
    <h1>WebSocket Chat</h1>
    <div>
      <input type="text" id="username" placeholder="Username">
      <input type="password" id="password" placeholder="Password">
      <button id="loginBtn" @click="tologin">Login</button>
    </div>
    <div id="chat" style="display: none;">
      <input type="text" id="message" placeholder="Type a message">
      <button id="sendBtn" @click="tosend">Send</button>
      <ul id="messages"></ul>
    </div>
  </div>
</template>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>
