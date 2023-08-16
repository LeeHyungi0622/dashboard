<template>
  <v-app
    id="inspire"
    class="font-sans"
    style="font-family: 'Nanum Gothic' !important"  >
    <v-system-bar
      app
      style="height: 7%; background-color: white; border-bottom: 1px solid #2b4f8c;"
    >
      <p class="appTitle text-base">Ingest Manager</p>
      <v-spacer></v-spacer>
      <div class="text-center">
        <v-menu offset-y>
          <template v-slot:activator="{ on, attrs }">
            <div class="userBox">
              <button class="pl-3" v-bind="attrs" v-on="on">
                <div class="flex">
                  <img src="@/assets/img/user.svg" alt="user" />
                  <div class="px-3">{{ `${userInfo['name']}(${userInfo['userId']})` }} 님</div>
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
              class="pl-3 cursor-pointer"
            >
              <img src="@/assets/img/user.svg" alt="user" />
              <v-list-item-title
                class="text-xs text-black pl-3"
              >
              {{ title }}
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
      <v-container fluid fill-height class="p-0">
        <v-layout justify-center class="block">
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
    <alert-popup
      v-if="delAlertShowFlag"
      :contents="delAlertContent"
      @close-alert-popup="closeDelAlertPopup"
    ></alert-popup>
    <temp-pipeline-popup
      v-if="tempPipelineShowFlag"
      @close-temp-pipeline-popup="closeTempPipelinePopup"
      :contents="tempPipelineContent"
    ></temp-pipeline-popup>
    <TaskListPopup
      v-if="tasklistShowFlag"
      :contents="taskAlertContent"
      @close-task-list-popup="closeTaskListPopup"
    ></TaskListPopup>
    <FailedConfirmPopup
      v-if="failedconfirmShowFlag"
      :contents="confirmContent"
      @close-failed-confirm-popup="closeFailedConfirmPopup"
    ></FailedConfirmPopup>
    <Loading />
  </v-app>
</template>

<script>
import CustomNavigation from "./components/nav/CustomNavigation.vue";
import ConfirmPopup from "./components/popup/ConfirmPopup.vue";
import FailedConfirmPopup from "./components/popup/FailedConfirmPopup.vue";
import UserAlertPopup from "./components/popup/UserAlertPopup.vue";
import AlertPopup from "./components/popup/AlertPopup.vue";
import EventBus from "@/eventBus/EventBus.js";
import UserInfo from "./js/api/user.js";
import PipelineList from "./js/api/pipelineList.js";
import TempPipelinePopup from "./components/popup/TempPipelinePopup.vue";
import TaskListPopup from "./components/popup/TaskListPopup.vue";
import tempPipeline from "./json/tempPipeline.json";
import Loading from "./components/loading/loadingBar.vue";
import Footer from "./components/footer/footer.vue";
export default {
  components: {
    CustomNavigation,
    UserAlertPopup,
    ConfirmPopup,
    FailedConfirmPopup,
    AlertPopup,
    TempPipelinePopup,
    Loading,
    Footer,
    TaskListPopup
  },
  computed: {
    activationRoutePath() {
      return this.$route.path;
    }
  },
  mounted(){
    UserInfo.getSecurityInfo()
        .then((res) => {
          let isSuccess = res.status === 200 || 201 || 204;
          if(isSuccess){
            if (res == "true"){
              if(this.getCookie("chaut") === ""){
      alert("로그인이 필요한 페이지 입니다.");
      location.reload();
    }
    else{
      UserInfo.getUserInfo()
        .then((res) => {
          let isSuccess = res.status === 200 || 201 || 204;
          if(isSuccess){
            this.userInfo = res;
            this.$store.state.userInfo = res;
          } 
          else {
            alert("사용자 정보를 불러오는데 실패했습니다.");
          }  
        })
        .catch((err) => {
          console.log("Fail to Get User Info", err);
        });
    }
            } else {
              UserInfo.getUserInfo()
        .then((res) => {
          let isSuccess = res.status === 200 || 201 || 204;
          if(isSuccess){
            this.userInfo = res;
            this.$store.state.userInfo = res;
          } 
          else {
            alert("사용자 정보를 불러오는데 실패했습니다.");
          }  
        })
        .catch((err) => {
          console.log("Fail to Get User Info", err);
        });
            }
          } 
          else {
            alert("보안 정보를 불러오는데 실패했습니다.");
          }  
        })
    

      
  },
  data() {
    return {
      tasklistShowFlag : false,
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
      delAlertContent: {
        title: "default",
        text: "default",
        url: "default",
        name: "default"
      },
      taskAlertContent:{
        data: [],
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
      failedconfirmShowFlag: false,
      delAlertShowFlag: false,
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
    EventBus.$on("show-task-list-popup", (payload) => {
      this.showTaskListPopup(payload);
    });
    EventBus.$on("show-failed-confirm-popup", (payload) => {
      this.showFailedConfirmPopup(payload);
    });
    EventBus.$on("show-del-pipeline-popup", (payload) => {
      this.showDelAlertPopup(payload);
    });

  },
  methods: {
    getCookie(cName) {
      cName = cName + '=';
      let cookieData = document.cookie;
      let start = cookieData.indexOf(cName);
      let cValue = '';
      if(start != -1){
        start += cName.length;
        let end = cookieData.indexOf(';', start);
        if(end == -1)end = cookieData.length;
        cValue = cookieData.substring(start, end);
      }
      return unescape(cValue);
  },
    closeConfirmPopup: function (val) {
      if(val.url == "update"){
        if(val.body == 'RUN'){
          PipelineList.putPipelineStatus(val.id, 'STOPPING').then((res) => {
            let isSuccess = res.status === 200 || 201 || 204;
            if(isSuccess){
              this.confirmShowFlag = false;
              let alertPayload = {
                  title: "정지 완료",
                  text:
                    val.name + " 파이프라인의 정지가 완료되었습니다.",
                  url: "completedUpdate",
                };
              EventBus.$emit("show-alert-popup", alertPayload);
            }

          }).catch((error) => error)
        }
        else{
          PipelineList.putPipelineStatus(val.id, 'STARTING').then((res) => {
            let isSuccess = res.status === 200 || 201 || 204;
            if(isSuccess){
              this.confirmShowFlag = false;
              let alertPayload = {
                  title: "실행 완료",
                  text:
                    val.name + " 파이프라인의 실행이 완료되었습니다.",
                  url: "completedUpdate",
                };
              EventBus.$emit("show-alert-popup", alertPayload);
            }

          }).catch((error) => error)
        }
      }
      else if(val.url == "deleteComplete"){
        PipelineList.deletePipeline(val.id).then((res) => {
            let isSuccess = res.status === 200 || 201 || 204;
            if(isSuccess){
              this.confirmShowFlag = false;
              let alertPayload = {
                  title: "삭제 완료",
                  text:
                    val.name + " 파이프라인의 삭제가 완료되었습니다.",
                  url: "pipelineDel",
                };
              EventBus.$emit("show-del-pipeline-popup", alertPayload);
              
            }

          }).catch((error) => error);
        
      }
      else if(val.url == "deleteTemp"){
        PipelineList.deleteTempPipeline(val.id).then((res) => {
          let isSuccess = res.status === 200 || 201 || 204;
          if(isSuccess){
            this.confirmShowFlag = false;
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
    closeDelAlertPopup: function () {
      this.delAlertShowFlag = false;
      location.reload();
    },
    showDelAlertPopup(payload) {
      this.delAlertShowFlag = true;
      this.delAlertContent.title = payload.title;
      this.delAlertContent.text = payload.text;
      this.delAlertContent.url = payload.url;
      this.delAlertContent.name = payload.name;
    },
    closeTempPipelinePopup: function () {
      this.tempPipelineShowFlag = false;
    },
    showTempPipelinePopup(payload) {
      this.tempPipelineShowFlag = true;
      this.tempPipelineContent.data = payload.contents;
    },
    closeTaskListPopup: function() {
      this.tasklistShowFlag = false;
    },
    showTaskListPopup: function(payload) {
      this.tasklistShowFlag = true;
      this.taskAlertContent.data = payload;
    },
    showFailedConfirmPopup: function(payload) {
      this.failedconfirmShowFlag = true;
      this.confirmContent.title = payload.title;
      this.confirmContent.text = payload.text;
      this.confirmContent.url = payload.url;
      this.confirmContent.param = payload.param;
      this.confirmContent.body = payload.body;
      this.confirmContent.id = payload.id;
    },
    closeFailedConfirmPopup: function() {
      this.failedconfirmShowFlag = false;
    },
    menuAction(mode) {
      if (mode == "userInfo") {
        this.showUserAlertPopup();
      } else {
        UserInfo.sendLogOut()
          .then(response => {
            const resultCode = response.status === 200 || 201 || 204;
            if (resultCode) {
              location.replace('/');
              location.reload(); 
            }
          });
      }
    },
  }
};
</script>

<style>
  /* expend table 아래 패딩 제거 */
  .v-application--wrap {
    min-height: 0vh !important;
  }
  /* pagination 1,2 ...page 크기 */
  .v-pagination__item {
    box-shadow: 0 3px 1px -2px rgb(0 0 0 / 20%), 0 2px 2px 0 rgb(0 0 0 / 14%), 0 1px 5px 0 rgb(0 0 0 / 12%);
    border-radius: 4px;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    text-decoration: none;
    height: 32px !important;
    width: 32px !important;
    margin: 0.3rem 10px;
}
/* pagination 한칸 앞뒤 크기 */
.v-pagination__navigation {
    box-shadow: 0 3px 1px -2px rgb(0 0 0 / 20%), 0 2px 2px 0 rgb(0 0 0 / 14%), 0 1px 5px 0 rgb(0 0 0 / 12%);
    border-radius: 4px;
    display: inline-
    ;
    justify-content: center;
    align-items: center;
    text-decoration: none;
    height: 32px !important;
    width: 32px !important;
    margin: 0.3rem 10px;
}
</style>
