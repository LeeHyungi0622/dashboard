<template>
  <div class="customTableMainArea">
    <div class="customTable" v-for="(content, key) in contents" :key="key">
      <div class="header fsb12" v-if="content.name == 'isBase64'">
        <p> Base64Decoder 설정 </p>
      </div>
      <div class="header fsb12" v-else-if="content.name == 'root_key'">
        <p> Root Key 설정 </p>
      </div>
      <div class="header fsb12" v-else-if="content.name != 'Scheduling'">
        <p v-if="content.name">{{ content.name }}</p>
        <button v-if="content.detail" @click="showTooltip(content.detail)">
          <img style="padding: 15px" src="../../assets/img/Help.svg" />
        </button>
      </div>
      
      <div class="value" v-if="content.name != 'Scheduling'">
        <!-- BASE64 DECODER -->
        <div
          class="disf"
          v-if="
          content.name == `isBase64` &&
            (tableUpdateFlag || $store.state.tableShowMode == `REGISTER`)
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
        <div v-else-if="tableUpdateFlag || $store.state.tableShowMode == `REGISTER`">
          <div v-if="Array.isArray(content.defaultValue)">
            <select
              class="disf"
              style="padding: 0px 20px 0px 20px;"
              v-if="content.defaultValue.length > 1"
              v-model="content.inputValue"
            >
              <option
                v-for="(item, key) in content.defaultValue"
                :key="key"
                :value="item"
              >
                {{ item }}
              </option>
            </select>
            <input v-else-if="content.name == 'Password'"  type="password" id="password" v-model="content.inputValue"/>
            <input v-else type="text" v-model="content.inputValue" />
          </div>
          <input v-else type="text" id="normal" v-model="content.inputValue" />
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
        <div style="padding-left: 20px" v-else-if="content.name == 'Password'">{{ showPasswordMasking(content.inputValue) }}</div>
        <div style="padding-left: 20px" v-else>{{ content.inputValue }}</div>
      </div>
    </div>
    <v-snackbar v-model="flag" light>
      {{showDetail}}
      <template v-slot:action="{ attrs }">
        <v-btn
          color="pink"
          text
          v-bind="attrs"
          @click="flag = false"
        >
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
export default {
  props: {
    contents: Array,
    tableUpdateFlag: Boolean
  },
  data() {
    return {
      flag : false,
      showDetail: "",
      password: ""
    };
  },
  mounted(){
  },
  methods:{
    showTooltip(val){
      this.showDetail = val;
      this.flag = true;
    },
    showPasswordMasking(password){
      return '*'.repeat(password.length);
    }
  }
};
</script>
