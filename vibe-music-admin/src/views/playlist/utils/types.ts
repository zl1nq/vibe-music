interface FormItemProps {
  /** 用于判断是`新增`还是`修改` */
  formTitle: string;
  playlistId?: number;
  title: string;
  introduction: string;
  style: string;
}
interface FormProps {
  formInline: FormItemProps;
}

export type { FormItemProps, FormProps };
