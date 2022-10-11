<template>
  <div class="customTableMainArea">
    <div class="customTable" v-for="(content, key) in contents" :key="key">
      <div class="header fsb12">
        <p v-if="content.name">{{ content.name }}</p>
        <button v-if="content.detail">
          <img style="padding: 15px" src="../../assets/img/Help.svg" />
        </button>
      </div>
      <div class="value">
        <!-- BASE64 DECODER -->
        <div
          class="disf"
          v-if="
            content.name == `isBase64` &&
            ($store.state.tableUpdateFlag || $store.state.tableShowMode == `REGISTER`)
          "
        >
          <input
            class="mgL12"
            type="radio"
            v-model="content.inputValue"
            value="true"
          />On
          <input
            type="radio"
            class="mgL12"
            v-model="content.inputValue"
            value="false"
          />Off
        </div>
        <!-- 수정이나 등록시 -->
        <div v-else-if="$store.state.tableUpdateFlag || $store.state.tableShowMode == `REGISTER`">
          <select
            style="padding: 0px 20px 0px 20px"
            v-if="Array.isArray(content.inputValue)"
            v-model="content.inputValue"
          >
            <option
              v-for="(item, key) in content.value.datas"
              :key="key"
              :value="item"
            >
              {{ item }}
            </option>
          </select>
          <select
            style="padding: 0px 20px 0px 20px"
            v-else-if="
              content.inputValue === 'true' || content.inputValue === 'false'
            "
            v-model="content.inputValue"
          >
            <option value="true">true</option>
            <option value="false">false</option>
          </select>
          <input v-else type="text" v-model="content.inputValue" />
        </div>
        <!-- BASE64인데 수정 아닐시 -->
        <div class="disf" v-else-if="content.name == `isBase64`">
          <input
            class="mgL12"
            type="radio"
            v-model="content.inputValue"
            :value="content.inputValue"
          />
          {{ content.inputValue == "true" ? "On" : "Off" }}
        </div>
        <!-- 수정 아닐시 -->
        <div style="padding-left: 20px" v-else>{{ content.inputValue }}</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    contents: Array
  },
  data() {
    return {
      showDetail : false
    };
  },
  methods:{
    showDetailPopup(){
      this.showDetail = !this.showDetail;

    }
  }
};
</script>
