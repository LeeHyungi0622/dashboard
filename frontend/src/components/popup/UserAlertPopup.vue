<template>
  <div class="text-center">
    <v-dialog v-model="dialog" persistent width="550">
      <v-card>
        <v-card-title class="lighten-2 text-sm font-bold" style="color: #2b4f8c">
          {{ contents.title }}
        </v-card-title>

        <v-card-text class="userAlertTextBox">
          <div style="border: 1px solid #dfdfdf">
            <div
              v-for="(item, key) in contents.userContent"
              :key="key"
              :class="key == 'id' ? 'first' : 'normal'"
            >
              <div class="key text-xs font-bold">{{ item.key }}</div>
              <div class="value text-xs">
                {{ item.value }}
              </div>
            </div>
          </div>
        </v-card-text>

        <v-card-actions class="justify-end">
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
          title: String,
          userContent: {
            userId: { key: String, value: String },
            name: { key: String, value: String },
            phone: { key: String, value: String },
          },
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
      this.$emit("close-userAlert-popup");
    },
  },
};
</script>
