<template>
  <div class="text-center">
    <v-dialog v-model="dialog" persistent width="500">
      <v-card>
        <v-card-title class="lighten-2 text-sm font-bold primary-color">
          {{ contents.title }}
        </v-card-title>

        <v-card-text
          class="text-sm h-44 flex justify-center items-center text-center" 
        >
          <div v-html="contents.text"></div>
        </v-card-text>

        <v-card-actions>
          <button class="text-sm" @click="closeConfirm">예</button>
          <button class="ml-3 text-sm" @click="closeCancel">아니요</button>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
export default {
  props: {
    contents: {
      type: Object,
      default: () => {
        return {
          title: null,
          text: null,
          url: null,
          param: null,
          body: null,
          id: null,
        };
      },
      required: true,
    },
  },
  data() {
    return {
      dialog: true,
    };
  },
  methods: {
    closeConfirm() {
      this.$emit("close-confirm-popup", this.contents);
    },
    closeCancel() {
      this.$emit("close-cancel-popup");
    },
  },
};
</script>
