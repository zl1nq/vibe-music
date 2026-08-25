import { reactive } from "vue";
import type { FormRules } from "element-plus";

/** 自定义表单规则校验 */
export const formRules = reactive(<FormRules>{
  artistName: [
    {
      validator: (rule, value, callback) => {
        if (value === "") {
          callback(new Error("歌手为必填项"));
        } else {
          callback();
        }
      },
      trigger: "blur"
    }
  ]
});
