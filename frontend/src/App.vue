<template>
  <v-app
    id="inspire"
    style="font-family: 'Nanum Gothic', sans-serif !important"
  >
    <v-system-bar
      app
      style="
        height: 7%;
        background-color: #ffff;
        border-bottom: 1px solid #2b4f8c;
      "
    >
      <p class="appTitle fs16">Ingest Manager</p>
      <v-spacer></v-spacer>
      <div class="text-center">
        <v-menu offset-y>
          <template v-slot:activator="{ on, attrs }">
            <div class="userBox">
              <button style="padding: 0 0 0 12px" v-bind="attrs" v-on="on">
                <div style="display: flex">
                  <img src="@/assets/img/user.svg" alt="user" />
                  <div style="padding: 0 10px 0 10px">홍길동(cityhub10)님</div>
                  <img src="@/assets/img/drop-down.svg" alt="user" />
                </div>
              </button>
            </div>
          </template>
          <v-list>
            <v-list-item
              v-for="([title, mode], key) in menu"
              :key="key"
              @click="menuAction(mode)"
              style="cursor: pointer; padding-left: 12px"
            >
              <img src="@/assets/img/user.svg" alt="user" />
              <v-list-item-title
                class="fs12"
                style="color: #000000; padding-left: 12px"
                v-text="title"
              >
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
    </v-system-bar>
    <v-navigation-drawer app style="top: 7%; background-color: #2b4f8c">
      <custom-navigation
        :activation-route-path="activationRoutePath"
      ></custom-navigation>
    </v-navigation-drawer>

    <v-main>
      <v-container fluid fill-height style="padding: 0">
        <v-layout justify-center style="display: block">
          <router-view></router-view>
          <footer></footer>
        </v-layout>
      </v-container>
    </v-main>
    <confirm-popup
      v-if="confirmShowFlag"
      :contents="confirmContent"
      @close-confirm-popup="closeConfirmPopup"
    ></confirm-popup>
    <user-alert-popup
      v-if="userAlertShowFlag"
      :contents="userAlertContent"
      @close-userAlert-popup="closeUserAlertPopup"
    ></user-alert-popup>
    <alert-popup
      v-if="alertShowFlag"
      :contents="alertContent"
      @close-alert-popup="closeAlertPopup"
    ></alert-popup>
    <temp-pipeline-popup
      v-if="tempPipelineShowFlag"
      @close-temp-pipeline-popup="closeTempPipelinePopup"
      :contents="tempPipelineContent"
    ></temp-pipeline-popup>
  </v-app>
</template>

<script>
import CustomNavigation from "./components/nav/CustomNavigation.vue";
import ConfirmPopup from "./components/popup/ConfirmPopup.vue";
import UserAlertPopup from "./components/popup/UserAlertPopup.vue";
import AlertPopup from "./components/popup/AlertPopup.vue";
import EventBus from "@/eventBus/EventBus.js";
// import userInfo from "./json/userInfo.json";
import userInfo from "./js/api/user.js";
import TempPipelinePopup from "./components/popup/TempPipelinePopup.vue";
import tempPipeline from "./json/tempPipeline.json";

export default {
  components: {
    CustomNavigation,
    UserAlertPopup,
    ConfirmPopup,
    AlertPopup,
    TempPipelinePopup,
  },
  computed: {
    activationRoutePath() {
      return this.$route.path;
    },
  },
  data() {
    return {
      confirmContent: {
        title: "default",
        text: "default",
        url: "default",
        param: "default",
        body: "default",
      },
      userInfo: {},
      userAlertContent: {
        title: "default",
        userContent: {
          userId: { key: null, value: null },
          name: { key: null, value: null },
          phone: { key: null, value: null },
        },
      },
      alertContent: {
        title: "입력값 오류",
        text: "입력값에 오류가 있습니다. <br/>구분자 혹은  값을 입력해주세요",
      },
      tempPipelineContent: {
        title: "임시저장 파이프라인",
        subTitle: " 파이프라인 목록",
        data: tempPipeline,
        filterList: [],
      },
      show: true,
      tempPipelineShowFlag: false,
      confirmShowFlag: false,
      userAlertShowFlag: false,
      alertShowFlag: false,
      menu: [
        ["사용자 정보", "userInfo"],
        ["로그아웃", "logout"],
      ],
    };
  },
  created() {
    EventBus.$on("show-confirm-popup", (payload) => {
      this.showconfirmPopup(payload);
    });
    EventBus.$on("show-temp-pipeline-popup", (payload) => {
      this.showTempPipelinePopup(payload);
    });
    userInfo.getUserInfo().then((res)=>{this.userInfo = res}).catch((err)=>{console.log("Fail to Get User Info", err)})
    console.log(this.userInfo);
  },
  methods: {
    closeConfirmPopup: function () {
      this.confirmShowFlag = false;
    },

    showconfirmPopup(payload) {
      this.confirmShowFlag = true;
      this.confirmContent.title = payload.title;
      this.confirmContent.text = payload.text;
      this.confirmContent.url = payload.url;
      this.confirmContent.param = payload.param;
      this.confirmContent.body = payload.body;
    },
    closeUserAlertPopup: function () {
      this.userAlertShowFlag = false;
    },
    showUserAlertPopup() {
      this.userAlertShowFlag = true;
      this.userAlertContent.title = "사용자 정보";
      this.userAlertContent.userContent.userId.key = "사용자 아이디";
      this.userAlertContent.userContent.userId.value = this.userInfo.userId;
      this.userAlertContent.userContent.name.key = "사용자 이름";
      this.userAlertContent.userContent.name.value = this.userInfo.name;
      this.userAlertContent.userContent.phone.key = "사용자 연락처";
      this.userAlertContent.userContent.phone.value = this.userInfo.phone;
    },
    closeAlertPopup: function () {
      this.alertShowFlag = false;
    },
    showAlertPopup() {
      this.userAlertShowFlag = true;
      this.alertContent.title = "입력값 오류";
      this.alertContent.text =
        "입력값에 오류가 있습니다. <br/>구분자 혹은  값을 입력해주세요";
    },
    closeTempPipelinePopup: function () {
      this.tempPipelineShowFlag = false;
    },
    showTempPipelinePopup(payload) {
      this.tempPipelineShowFlag = true;
      this.tempPipelineContent.data = payload.contents;
    },
    menuAction(mode) {
      if (mode == "userInfo") {
        this.showUserAlertPopup();
      } else {
        console.log("logout!!!!");
      }
    },
  },
};
</script>

<style></style>
