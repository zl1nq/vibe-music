interface FormItemProps {
  /** 用于判断是`新增`还是`修改` */
  title: string;
  userId: number;
  username: string;
  password: string;
  phone: string | number;
  email: string;
  userStatus: number;
  introduction: string;
}
interface FormProps {
  formInline: FormItemProps;
}

export type { FormItemProps, FormProps };
