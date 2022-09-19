<template>
  <div class="customTableMainArea">
    <div class="customTable" v-for="(content, key) in contents" :key="key">
      <div class="header fsb12">
        <p v-if="content.key">{{ content.key }}</p>
        <button v-if="content.desc">
          <img style="padding: 15px" src="../../assets/img/Help.svg" />
        </button>
      </div>
      <div class="value">
        <div
          class="disf"
          v-if="content.key == `Base64 Decoder` && mode == `UPDATE`"
        >
          <input
            class="mgL12"
            type="radio"
            v-model="content.value"
            value="On"
          />On
          <input
            type="radio"
            class="mgL12"
            v-model="content.value"
            value="Off"
          />Off
        </div>
        <div v-else-if="mode == `UPDATE`">
          <input type="text" v-model="content.value" />
        </div>
        <div class="disf" v-else-if="content.key == `Base64 Decoder`">
          <input
            class="mgL12"
            type="radio"
            v-model="content.value"
            :value="content.value"
          />
          {{ content.value }}
        </div>

        <div style="padding-left: 20px" v-else>{{ content.value }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    contents: Array,
    mode: String,
  },
};
</script>
