<template>
  <div class="text-center">
    <v-dialog v-model="dialog" persistent width="500">
      <v-card>
        <v-card-title class="lighten-2 text-sm font-bold" style="color: #2b4f8c">
          {{ contents.title }}
        </v-card-title>

        <v-card-text
          style="
            height: 180px;
            display: flex;
            justify-content: center;
            align-items: center;
            text-align: center;
          "
          class="text-sm"
        >
          <div v-html="contents.text"></div>
        </v-card-text>

        <v-card-actions style="justify-content: center">
          <button class="text-sm" @click="close">확인</button>
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
    close() {
      this.$emit("close-alert-popup");
    },
  },
};
</script>
