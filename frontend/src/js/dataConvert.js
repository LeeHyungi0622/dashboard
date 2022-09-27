import pipelineData from "../json/pipeLineEdit.json";

export default {
  getContents(contentsName) {
    return pipelineData[contentsName].NifiComponents;
  },
  getConverterContents() {
    return this.getContents("converter").filter(
      (item) => item.name == "DataSetProperties"
    );
  },
  getIdContents() {
    return this.getContents("converter").filter(
      (item) => item.name == "IDGenerater"
    );
  },
  defaultInfoContents() {
    return [
      {
        name: "파이프라인 이름",
        inputValue: this.pipelineData.name,
      },
      {
        name: "파이프라인 정의",
        inputValue: this.pipelineData.detail,
      },
    ];
  },
};
