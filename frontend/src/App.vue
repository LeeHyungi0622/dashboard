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
                  <div style="padding: 0 10px 0 10px">{{displayUser}}</div>
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
          <router-view :key="$route.fullPath"></router-view>
          <v-footer
          height="150"
          color="white"
          app
          inset
          >
          <img src="./assets/img/DHub.svg" style="height: 40px;" />
          <span style="padding: 20px">
            디토닉㈜
            <br/> 
            대표이사 : 전용주  |  주소 : 경기도 안양시 동안구 시민대로 248번길 25(관양동, 경기창조산업안양센터)
            <br/> 
            사업자등록번호 : 264-81-37380
            <br/> 
            대표전화 : 031-689-4770     |     팩스 : 031-422-1161
            <br/> 
            COPYRIGHT &copy; Dtonic Dtonic CO. LTD. All RIGHTS RESERVED.</span>
        </v-footer>
        </v-layout>
      </v-container>
    </v-main>
    <confirm-popup
      v-if="confirmShowFlag"
      :contents="confirmContent"
      @close-confirm-popup="closeConfirmPopup"
      @close-cancel-popup="closeCancelPopup"
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
import UserInfo from "./js/api/user.js";
import PipelineList from "./js/api/pipelineList.js";
import TempPipelinePopup from "./components/popup/TempPipelinePopup.vue";
import tempPipeline from "./json/tempPipeline.json";

export default {
  components: {
    CustomNavigation,
    UserAlertPopup,
    ConfirmPopup,
    AlertPopup,
    TempPipelinePopup
  },
  computed: {
    activationRoutePath() {
      return this.$route.path;
    },
    displayUser(){
      return this.$store.state.userInfo.name +"(" +this.$store.state.userInfo.userId+")님";
    }
  },
  data() {
    return {
      confirmContent: {
        title: "default",
        text: "default",
        url: "default",
        param: "default",
        body: "default",
        id: "default"
      },
      alertContent: {
        title: "default",
        text: "default",
        url: "default"
      },
      userAlertContent: {
        title: "default",
        userContent: {
          userId: { key: null, value: null },
          name: { key: null, value: null },
          phone: { key: null, value: null }
        }
      },
      tempPipelineContent: {
        title: "임시저장 파이프라인",
        subTitle: " 파이프라인 목록",
        data: tempPipeline,
        filterList: []
      },
      show: true,
      tempPipelineShowFlag: false,
      confirmShowFlag: false,
      userAlertShowFlag: false,
      alertShowFlag: false,
      menu: [
        ["사용자 정보", "userInfo"],
        ["로그아웃", "logout"]
      ]
    };
  },
  created() {
    EventBus.$on("show-alert-popup", (payload) => {
      this.showAlertPopup(payload);
    });
    EventBus.$on("show-confirm-popup", (payload) => {
      this.showconfirmPopup(payload);
    });
    EventBus.$on("show-temp-pipeline-popup", (payload) => {
      this.showTempPipelinePopup(payload);
    });
    UserInfo.getUserInfo()
      .then((res) => {
        this.$store.state.userInfo.name = res.name;
        this.$store.state.userInfo.userId = res.userId;
        this.$store.state.userInfo.nickName = res.nickName;
        this.$store.state.userInfo.phone = res.phone;
      })
      .catch((err) => {
        console.log("Fail to Get User Info", err);
      });
  },
  methods: {
    closeConfirmPopup: function (val) {
      if(val.url == "update"){
        PipelineList.putPipelineStatus(val.id, val.body)
      }
      else if(val.url == "deleteComplete"){
        PipelineList.deletePipeline(val.id);
        this.$router.go();
      }
      else if(val.url == "deleteTemp"){
        PipelineList.deleteTempPipeline(val.id).then((res) => {
          let isSuccess = res.status === 200 || 201 || 204;
          if(isSuccess){
            PipelineList.getTempPipelineList()
            .then((res) => {
              this.$store.state.tempPipelineList = res;
            })
            .catch((error) => error);
          }
        })
        .catch((error) => error);
      }
      this.confirmShowFlag = false;
    },
    closeCancelPopup: function () {
      this.confirmShowFlag = false;
    },

    showconfirmPopup(payload) {
      this.confirmShowFlag = true;
      this.confirmContent.title = payload.title;
      this.confirmContent.text = payload.text;
      this.confirmContent.url = payload.url;
      this.confirmContent.param = payload.param;
      this.confirmContent.body = payload.body;
      this.confirmContent.id = payload.id;
    },
    closeUserAlertPopup: function () {
      this.userAlertShowFlag = false;
    },
    showUserAlertPopup() {
      this.userAlertShowFlag = true;
      this.userAlertContent.title = "사용자 정보";
      this.userAlertContent.userContent.userId.key = "사용자 아이디";
      this.userAlertContent.userContent.userId.value = this.$store.state.userInfo.userId;
      this.userAlertContent.userContent.name.key = "사용자 이름";
      this.userAlertContent.userContent.name.value = this.$store.state.userInfo.name;
      this.userAlertContent.userContent.phone.key = "사용자 연락처";
      this.userAlertContent.userContent.phone.value = this.$store.state.userInfo.phone;
    },
    closeAlertPopup: function () {
      this.alertShowFlag = false;
    },
    showAlertPopup(payload) {
      this.alertShowFlag = true;
      this.alertContent.title = payload.title;
      this.alertContent.text = payload.text;
      this.alertContent.url = payload.url;
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
        UserInfo.sendLogOut()
        .then((res) => {
          let isSuccess = res.status === 200 || 201 || 204;
          if(isSuccess){
            location.replace('/');
          }
      })
      .catch((err) => {
        console.log("Fail to logout", err);
      });
      }
    }
  }
};
</script>

<style></style>
