<template>
  <div>
    <h1>Chat</h1>
    <div>
      <input v-model="message" placeholder="Type a message" />
      <input v-model="targetUserId" placeholder="Target User ID" />
      <button @click="sendMessage">Send</button>
    </div>
    <ul>
      <li v-for="(msg, index) in messages" :key="index">{{ msg }}</li>
    </ul>
  </div>
</template>

<script>
export default {
  data() {
    return {
      message: '',
      targetUserId: '',
      messages: [],
      socket: null,
    };
  },
  mounted() {
    this.initWebSocket();
  },
  beforeUnmount() {
    if (this.socket) {
      this.socket.close();
    }
  },
  methods: {
    initWebSocket() {
      const token = localStorage.getItem('token');
      this.socket = new WebSocket(`ws://localhost:8088/ws`);

      this.socket.onopen = () => {
        console.log('WebSocket 连接成功');
        // 发送 token
        this.socket.send(token);
      };

      this.socket.onmessage = (event) => {
        this.messages.push(event.data); // 接收消息并显示
      };

      this.socket.onclose = () => {
        console.warn('WebSocket 连接关闭:', event);
      };

      this.socket.onerror = (error) => {
        console.error('WebSocket 连接错误:', error);
      };
    },
    sendMessage() {
      console.log(this.socket)
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        const payload = {
          type: 'private',
          targetUserId: this.targetUserId,
          content: this.message,
        };
        this.socket.send(JSON.stringify(payload));
        this.message = ''; // 清空输入框
      } else {
        alert('WebSocket is not connected');
      }
    },
  },
};
</script>