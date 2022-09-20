<template>
  <v-list>
    <v-list-group :value="true" no-action sub-group>
      <template v-slot:activator>
        <v-list-item-content>
          <v-list-item-title class="pipelineManagement fs12"
            >데이터 파이프라인 관리</v-list-item-title
          >
        </v-list-item-content>
      </template>

      <v-list-item
        v-for="([title, path], i) in admins"
        :class="path == activationRoutePath ? `activationNav` : ``"
        :key="i"
        link
        @click="moveRoute(path)"
      >
        <v-list-item-title
          style="color: white"
          class="fsb12"
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
      ["파이프라인 목록", "/"],
      ["파이프라인 등록", "/pipelineCreate"],
    ],
  }),
  methods: {
    moveRoute(path) {
      if (this.activationRoutePath != path) {
        this.$router.push(path);
      }
    },
  },
};
</script>
