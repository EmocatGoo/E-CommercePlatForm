/* eslint-disable */
/**
 * 自动生成
 */

export interface Result<T> {
    code?: string;
    msg?: string;
    data?: T;
}

export interface PageBean<T> {
    total?: string;
    records?: any[];
}

export interface PageQuery {
    keyword?: string;
    page?: string;
    pageSize?: string;
}

export interface AdminDTO {
    username?: string;
    avatar?: string;
    name?: string;
}

export interface LoginDTO {
    username?: string;
    password?: string;
    phone?: string;
    loginType?: string;
}

export interface HttpServletRequest {
}

export interface ArticleQuery {
    articleTitle?: string;
    page?: string;
    pageSize?: string;
}

export interface ArticleReviewDTO {
    id?: string;
    authorType?: string;
    authorId?: string;
    status?: string;
    refuseReason?: string;
}

export interface AddCartDTO {
    productId?: string;
    quantity?: string;
    isChecked?: boolean;
}

export interface CartVO {
    itemCount?: string;
    checkedCount?: string;
    items?: CartItemVO[];
}

export interface CartItemVO {
    productId?: string;
    productName?: string;
    image?: string;
    priceAtAdd?: string;
    currentPrice?: string;
    quantity?: string;
    checked?: boolean;
    savePrice?: string;
}

export interface CategoryQuery {
    categoryName?: string;
    tag?: string;
    page?: string;
    pageSize?: string;
}

export interface ResponseEntity<T> {
    status?: HttpStatusCode;
}

export interface HttpStatusCode {
}

export interface CraftsmanQuery {
    name?: string;
    page?: string;
    pageSize?: string;
}

export interface CraftsmanAuthDTO {
    craftsmanId?: string;
    realName?: string;
    idNumber?: string;
    phone?: string;
    email?: string;
    technique?: string;
    introduction?: string;
    handleCard?: string;
    idCardFront?: string;
    idCardBack?: string;
    proofImages?: string[];
    masterpieceImages?: string[];
}

export interface CraftsmanReviewDTO {
    authId?: string;
    craftsmanId?: string;
    status?: string;
    rejectReason?: string;
}

export interface PasswordDTO {
    oldPassword?: string;
    newPassword?: string;
}

export interface CraftsmanDTO {
    name?: string;
    username?: string;
    password?: string;
    email?: string;
    idNumber?: string;
    skillCategory?: string;
    phone?: string;
    bio?: string;
    avatar?: string;
    updateTime?: LocalDate;
}

export interface LocalDate {
    year?: string;
    month?: string;
    day?: string;
}

export interface EmailDTO {
    email?: string;
    verifycode?: string;
}

export interface HttpSession {
}

export interface EventDTO {
    id?: string;
    title?: string;
    coverImage?: string;
    eventDetail?: string;
    description?: string;
    eventType?: string;
    status?: string;
    startTime?: LocalDateTime;
    endTime?: LocalDateTime;
    location?: string;
    maxPeople?: string;
    organizerId?: string;
}

export interface LocalDateTime {
    date?: LocalDate;
    time?: LocalTime;
}

export interface LocalTime {
    hour?: string;
    minute?: string;
    second?: string;
    nano?: string;
}

export interface EventApplyDTO {
    eventId?: string;
    realName?: string;
    phone?: string;
    idNumber?: string;
}

export interface EventQuery {
    title?: string;
    eventType?: string;
    status?: string;
    startTime?: LocalDateTime;
    endTime?: LocalDateTime;
    page?: string;
    pageSize?: string;
}

export interface EventCommentAddDTO {
    eventId?: string;
    content?: string;
    parentCommentId?: string;
    replyToUserId?: string;
    replyToUsername?: string;
    imageUrl?: string[];
}

export interface HttpServletResponse {
}

export interface OrderQuery {
    orderSn?: string;
    consignee?: string;
    orderStatus?: string;
    beginTime?: LocalDateTime;
    endTime?: LocalDateTime;
    page?: string;
    pageSize?: string;
}

export interface RefundQuery {
    orderSn?: string;
    orderStatus?: string;
    craftsmanId?: string;
    beginTime?: LocalDateTime;
    endTime?: LocalDateTime;
    page?: string;
    pageSize?: string;
}

export interface OrderReviewDTO {
    orderSn?: string;
    status?: string;
    rejectReason?: string;
}

export interface OrderStatsuDTO {
    status?: string;
    paySn?: string;
    expressNo?: string;
    expressCompany?: string;
}

export interface UserSignUpRefundDTO {
    paySn?: string;
    productIds?: string[];
    refundReason?: string;
    refundDesc?: string;
    refundImage?: string[];
    refundType?: string;
}

export interface OrderUserVO {
    totalAmount?: string;
    paySn?: string;
    orderStatus?: string;
    createTime?: LocalDateTime;
    payTime?: LocalDateTime;
    paymentMethod?: string;
    consignee?: string;
    shippingAddress?: string;
    phone?: string;
    expressCompany?: string;
    expressNo?: string;
    orderSn?: string[];
    items?: OrderItemUserVO[];
}

export interface OrderItemUserVO {
    productId?: string;
    productName?: string;
    productImage?: string;
    quantity?: string;
    price?: string;
    isCommented?: string;
    refundStatus?: string;
}

export interface AlipayTradeFastpayRefundQueryResponse {
    httpBody?: string;
    code?: string;
    msg?: string;
    subCode?: string;
    subMsg?: string;
    errorCode?: string;
    gmtRefundPay?: string;
    industrySepcDetail?: string;
    outRequestNo?: string;
    outTradeNo?: string;
    presentRefundBuyerAmount?: string;
    presentRefundDiscountAmount?: string;
    presentRefundMdiscountAmount?: string;
    refundAmount?: string;
    refundChargeAmount?: string;
    refundDetailItemList?: TradeFundBill[];
    refundReason?: string;
    refundRoyaltys?: RefundRoyaltyResult[];
    refundSettlementId?: string;
    refundStatus?: string;
    sendBackFee?: string;
    totalAmount?: string;
    tradeNo?: string;
}

export interface TradeFundBill {
    fundChannel?: string;
    bankCode?: string;
    amount?: string;
    realAmount?: string;
    fundType?: string;
}

export interface RefundRoyaltyResult {
    refundAmount?: string;
    royaltyType?: string;
    resultCode?: string;
    transOut?: string;
    transOutEmail?: string;
    transIn?: string;
    transInEmail?: string;
}

export interface ProductCommentLikeDTO {
    productId?: string;
    commentId?: string;
    userId?: string;
}

export interface CommentQuery {
    keyWordComment?: string;
    page?: string;
    pageSize?: string;
}

export interface ProductDTO {
    id?: string;
    productName?: string;
    price?: string;
    description?: string;
    categoryId?: string;
    stock?: string;
    imageUrl?: string;
    culturalBackground?: string;
    status?: string;
    rejectReason?: string;
}

export interface ProductListVO {
    id?: string;
    productName?: string;
    price?: string;
    imageUrl?: string;
    craftsmanName?: string;
    culturalBackground?: string;
}

export interface ProductQuery {
    keyword?: string;
    categoryId?: string;
    status?: string;
    page?: string;
    pageSize?: string;
}

export interface ProductDetailVO {
    description?: string;
    craftsmanIntro?: string;
    workshopName?: string;
}

export interface QuestionQuery {
    keyword?: string;
    questionType?: string;
    page?: string;
    pageSize?: string;
}

export interface QuestionRecordDTO {
    userId?: string;
    questionId?: string;
    answer?: string;
}

export interface UserAddressDTO {
    id?: string;
    userId?: string;
    receiverName?: string;
    receiverPhone?: string;
    province?: string;
    city?: string;
    district?: string;
    detailAddress?: string;
    createTime?: LocalDateTime;
    updateTime?: LocalDateTime;
}

export interface UserUpdateDTO {
    name?: string;
    email?: string;
    phone?: string;
    idNumber?: string;
}

export interface UserRegisterDTO {
    username?: string;
    phone?: string;
    password?: string;
    email?: string;
}

export interface WorkShopDTO {
    workshopName?: string;
    description?: string;
    location?: string;
    workshopLogo?: string;
    coverImage?: string;
    story?: string;
    techniqueIntro?: string;
    shortVideos?: VideoVO[];
}

export interface VideoVO {
    title?: string;
    url?: string;
    cover?: string;
    duration?: string;
}

