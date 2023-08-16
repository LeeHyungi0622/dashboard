<template>
  <v-list>
    <v-list-group :value="true" no-action sub-group>
      <template v-slot:activator>
        <v-list-item-content>
          <v-list-item-title class="pipelineManagement text-xs"
            >데이터 파이프라인 관리</v-list-item-title
          >
        </v-list-item-content>
      </template>

      <v-list-item
        v-for="([title, path], i) in admins"
        :class="activationRoutePath.includes(path) ? `activationNav` : ``"
        :key="i"
        link
        @click="moveRoute(path)"
        :disabled="path == activationRoutePath"
      >
        <v-list-item-title
          class="text-xs font-bold text-white"
          v-text="title"
        ></v-list-item-title>
      </v-list-item>
    </v-list-group>
  </v-list>
</template>
<script>
export default {
  props: {
    activationRoutePath: String,
  },
  data: () => ({
    admins: [
      ["파이프라인 목록", "/list"],
      ["파이프라인 등록", "/pipelineRegister"],
    ],
  }),
  methods: {
    moveRoute(path) {

      if (this.activationRoutePath != path) {
      if(path == "/pipelineRegister"){
        this.$store.state.tableShowMode = "REGISTER";
        this.$router.push(path + "/new");
      } else {
        this.$store.state.tableShowMode = "REGISTER";
        this.$router.push(path);
      }
      }
    },
  },
};
</script>
