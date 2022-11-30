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
                  <div style="padding: 0 10px 0 10px">{{ `${userInfo['name']}(${userInfo['userId']})` }} 님</div>
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
        </v-layout>
      </v-container>
    </v-main>
    <Footer/>
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
    <Loading />
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
import Loading from "./components/loading/loadingBar.vue";
import Footer from "./components/footer/footer.vue";
export default {
  components: {
    CustomNavigation,
    UserAlertPopup,
    ConfirmPopup,
    AlertPopup,
    TempPipelinePopup,
    Loading,
    Footer
  },
  computed: {
    activationRoutePath() {
      return this.$route.path;
    }
  },
  mounted(){
    UserInfo.getUserInfo()
      .then((res) => {
        this.userInfo = res;
        this.$store.state.userInfo = res;
      })
      .catch((err) => {
        console.log("Fail to Get User Info", err);
      });
  },
  data() {
    return {
      confirmContent: {
        title: "default",
        text: "default",
        url: "default",
        param: "default",
        body: "default",
        id: "default",
        name: "default"
      },
      alertContent: {
        title: "default",
        text: "default",
        url: "default",
        name: "default"
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
      ],
      overlay: false,
      userInfo: {}
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

  },
  methods: {
    set_cookie(name, value, unixTime) {
      var date = new Date();
      date.setTime(date.getTime() + unixTime);
      document.cookie = encodeURIComponent(name) + '=' + encodeURIComponent(value) + ';expires=' + date.toUTCString() + ';path=/';
    },
    closeConfirmPopup: function (val) {
      if(val.url == "update"){
        if(val.body == 'RUN'){
          PipelineList.putPipelineStatus(val.id, 'STOPPING').then((res) => {
            let isSuccess = res.status === 200 || 201 || 204;
            if(isSuccess){
              let alertPayload = {
                  title: "정지 완료",
                  text:
                    val.name + " 파이프라인의 정지가 완료되었습니다.",
                  url: "completedUpdate",
                };
              EventBus.$emit("show-alert-popup", alertPayload);
              this.$router.go();
            }

          }).catch((error) => error)
        }
        else{
          PipelineList.putPipelineStatus(val.id, 'STARTING').then((res) => {
            let isSuccess = res.status === 200 || 201 || 204;
            if(isSuccess){
              let alertPayload = {
                  title: "실행 완료",
                  text:
                    val.name + " 파이프라인의 실행이 완료되었습니다.",
                  url: "completedUpdate",
                };
              EventBus.$emit("show-alert-popup", alertPayload);
              this.$router.go();
            }

          }).catch((error) => error)
        }
      }
      else if(val.url == "deleteComplete"){
        PipelineList.deletePipeline(val.id).then((res) => {
            let isSuccess = res.status === 200 || 201 || 204;
            if(isSuccess){
              let alertPayload = {
                  title: "삭제 완료",
                  text:
                    val.name + " 파이프라인의 삭제가 완료되었습니다.",
                  url: "completedUpdate",
                };
              EventBus.$emit("show-alert-popup", alertPayload);
              this.$router.go();
            }

          }).catch((error) => error)
        
      }
      else if(val.url == "deleteTemp"){
        PipelineList.deleteTempPipeline(val.id).then((res) => {
          let isSuccess = res.status === 200 || 201 || 204;
          if(isSuccess){
            PipelineList.getTempPipelineList()
            .then((res) => {
              this.$store.state.tempPipelineList = res;
              this.tempPipelineShowFlag = false;
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
      this.confirmContent.name = payload.name;
    },
    closeUserAlertPopup: function () {
      this.userAlertShowFlag = false;
    },
    showUserAlertPopup() {
      this.userAlertShowFlag = true;
      this.userAlertContent.title = "사용자 정보";
      this.userAlertContent.userContent.userId.key = "사용자 아이디";
      this.userAlertContent.userContent.userId.value = this.userInfo['userId'];
      this.userAlertContent.userContent.name.key = "사용자 이름";
      this.userAlertContent.userContent.name.value = this.userInfo['name'];
      this.userAlertContent.userContent.phone.key = "사용자 연락처";
      this.userAlertContent.userContent.phone.value = this.userInfo['phone'];
    },
    closeAlertPopup: function () {
      this.alertShowFlag = false;
    },
    showAlertPopup(payload) {
      this.alertShowFlag = true;
      this.alertContent.title = payload.title;
      this.alertContent.text = payload.text;
      this.alertContent.url = payload.url;
      this.alertContent.name = payload.name;
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
          .then(response => {
            console.log(response);
            const resultCode = response.status === 200 || 201 || 204;
            if (resultCode) {
              location.replace('/');
              location.reload(); 
            }
          });
      }
    }
  }
};
</script>

<style></style>
