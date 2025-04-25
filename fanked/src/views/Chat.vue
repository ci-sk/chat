<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const message = ref("");
const targetUserId = ref("");
const messages = ref([]);
const socket = ref(null);
const isConnected = ref(false);

onMounted(() => {
  initWebSocket();
});

onBeforeUnmount(() => {
  if (socket.value) {
    socket.value.close();
  }
});

function initWebSocket() {
  const token = localStorage.getItem("token");
  socket.value = new WebSocket(`ws://localhost:8088/ws`);

  socket.value.onopen = () => {
    console.log("WebSocket 连接成功");
    isConnected.value = true;
    // 发送 token
    socket.value.send(token);
  };

  socket.value.onmessage = (event) => {
    messages.value.push(event.data); // 接收消息并显示
  };

  socket.value.onclose = () => {
    console.warn("WebSocket 连接关闭");
    isConnected.value = false;
  };

  socket.value.onerror = (error) => {
    console.error("WebSocket 连接错误:", error);
    isConnected.value = false;
  };
}

function sendMessage() {
  if (!message.value.trim()) return;

  if (socket.value && socket.value.readyState === WebSocket.OPEN) {
    const payload = {
      type: "private",
      targetUserId: targetUserId.value,
      content: message.value,
    };
    socket.value.send(JSON.stringify(payload));
    message.value = ""; // 清空输入框
  } else {
    alert("WebSocket 未连接");
  }
}

function isMyMessage(msg) {
  // 这里可以根据实际情况判断消息是否为自己发送的
  // 例如，可以检查消息对象中的发送者ID
  return Math.random() > 0.5; // 示例：随机判断
}

function getCurrentTime() {
  const now = new Date();
  return `${now.getHours().toString().padStart(2, "0")}:${now
    .getMinutes()
    .toString()
    .padStart(2, "0")}`;
}

function logout() {
  localStorage.removeItem("token");
  router.push("/login");
}
</script>

<template>
  <div class="min-h-screen bg-gray-100">
    <div class="container mx-auto px-4 py-6 max-w-4xl">
      <!-- 头部 -->
      <div
        class="bg-white rounded-lg shadow-md p-4 mb-6 flex justify-between items-center"
      >
        <h1 class="text-2xl font-bold text-indigo-600">聊天室</h1>
        <button
          @click="logout"
          class="px-4 py-2 bg-red-500 rounded-md hover:bg-red-600 transition-colors"
        >
          退出登录
        </button>
      </div>

      <!-- 聊天区域 -->
      <div class="bg-white rounded-lg shadow-md overflow-hidden">
        <!-- 消息列表 -->
        <div class="h-96 overflow-y-auto p-4 bg-gray-50">
          <div
            v-if="messages.length === 0"
            class="flex items-center justify-center h-full text-gray-400"
          >
            暂无消息，开始聊天吧
          </div>
          <ul v-else class="space-y-3">
            <li
              v-for="(msg, index) in messages"
              :key="index"
              class="p-3 rounded-lg max-w-3/4"
              :class="{
                'bg-indigo-100 ml-auto': isMyMessage(msg),
                'bg-gray-200': !isMyMessage(msg),
              }"
            >
              <div class="font-medium text-xs text-gray-500 mb-1">
                {{ isMyMessage(msg) ? "我" : "对方" }} · {{ getCurrentTime() }}
              </div>
              {{ msg }}
            </li>
          </ul>
        </div>

        <!-- 输入区域 -->
        <div class="p-4 border-t border-gray-200">
          <div class="flex mb-3">
            <input
              v-model="targetUserId"
              placeholder="接收者ID"
              class="w-1/4 px-4 py-2 border border-gray-300 rounded-l-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            />
            <input
              v-model="message"
              placeholder="输入消息..."
              class="flex-1 px-4 py-2 border-y border-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              @keyup.enter="sendMessage"
            />
            <button
              @click="sendMessage"
              class="px-6 py-2 bg-indigo-600 text-white rounded-r-md hover:bg-indigo-700 transition-colors flex items-center"
              :disabled="!message.trim()"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5 mr-1"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z"
                />
              </svg>
              发送
            </button>
          </div>

          <!-- 连接状态 -->
          <div class="text-sm text-gray-500 flex items-center">
            <span
              class="w-2 h-2 rounded-full mr-2"
              :class="{
                'bg-green-500': isConnected,
                'bg-red-500': !isConnected,
              }"
            ></span>
            {{ isConnected ? "已连接" : "未连接" }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
