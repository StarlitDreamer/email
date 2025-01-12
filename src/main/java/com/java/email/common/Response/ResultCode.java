package com.java.email.Response;

public enum ResultCode {
    //成功
    R_Ok(200, "操作成功"),
    //失败
    R_Fail(400, "操作失败"),
    //异常
    R_Error(500, "操作异常"),

    R_TimeOut(600,"超时"),
    //-----------------------系统内部错误(不应该出现但防止出现) 501-600---------------------
    R_WhyNull(501, "不可能NULL的数据为NULL了"),
    R_UpdateDbFailed(502, "修改数据库失败"),



    //-----------------------用户相关错误 401-500---------------------
    R_ParamError(401,"参数异常"),
    R_UserNameIsExist(402,"用户名已存在"),
    R_UserPhoneNumberIsExist(403, "手机号已注册"),
    R_UserEmailIsExist(404,"邮箱已被注册"),
    R_PasswordError(405,"密码错误"),
    R_UserNotFound(406,"该用户不存在"),
    R_OldPasswordError(407,"旧密码错误"),
    R_NewPasswordNotSame(408,"新密码不一致"),
    R_RoleAlreadyUpgrade(409,"权限早已升级"),
    R_CodeError(410,"验证码错误"),
    R_CampIsExist(411,"该营地已存在，请检查名称或地址是否正确"),
    R_DateError(412,"预定时间错误"),
    R_CampNotFound(413,"营地不存在"),
    R_CampAlreadyReserved(414,"该营地不可预定"),
    R_PriceIsLow(415,"预定价格过低"),
    R_IsReserved(416,"用户已有预定"),
    R_UserNotReserved(417,"用户暂无预定"),
    R_VehicleNotFound(418,"车辆不存在"),
    R_VehicleAlreadyReserved(419,"该车辆不可预定"),
    R_ReservationNotFound(420,"未找到该预定信息"),
    R_UserNoBalance(421,"该用户余额不足"),
    R_SignedContractFailed(422,"签署合同失败"),
    R_ReservationTimeout(424,"该预定已超时"),
    R_PaymentFailed(425,"支付失败"),
    R_ExceedRefundTime(426,"超出退款时间"),
    R_PaymentNotFound(427,"未找到该订单信息"),
    R_ExceedCancelTime(428,"超出退款时间"),
    R_InvoiceAlreadyExist(429,"该订单发票已开具"),
    R_PaymentNotSuccess(429,"该订单未支付成功，不可开具发票"),
    R_PaymentNotGenerateInvoice(430,"该订单暂未生成发票"),






    //-----------------------系统相关错误 401-500---------------------
    R_SaveFileError(501,"文件保存异常"),
    ;

    private int code;
    private String msg;
    ResultCode(int code,String msg){
        this.code=code;
        this.msg=msg;
    }
    public int getCode(){
        return code;
    }
    public String getMsg(){
        return msg;
    }
}
