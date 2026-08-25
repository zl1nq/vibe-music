import { reactive } from "vue";
import type { FormRules } from "element-plus";

/** 自定义表单规则校验 */
export const formRules = reactive(<FormRules>{
  songName: [
    {
      validator: (rule, value, callback) => {
        if (value === "") {
          callback(new Error("歌名为必填项"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ],
  album: [
    {
      validator: (rule, value, callback) => {
        if (value === "") {
          callback(new Error("专辑为必填项"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ],
  releaseTime: [
    {
      validator: (rule, value, callback) => {
        if (value === "") {
          callback(new Error("发行日期为必填项"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
});
