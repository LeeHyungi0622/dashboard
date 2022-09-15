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
  </v-app>
</template>

<script>
import CustomNavigation from "./components/nav/CustomNavigation.vue";
import ConfirmPopup from "./components/popup/ConfirmPopup.vue";
import EventBus from "@/eventBus/EventBus.js";

export default {
  components: {
    CustomNavigation,
    ConfirmPopup,
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
      show: true,
      confirmShowFlag: false,
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
    menuAction(mode) {
      if (mode == "userInfo") {
        console.log("userInfo!!!!");
      } else {
        console.log("logout!!!!");
      }
    },
  },
};
</script>

<style></style>
