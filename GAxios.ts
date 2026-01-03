/* eslint-disable */
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import axios from 'axios'
import type { Result, PageBean, PageQuery, AdminDTO, LoginDTO, HttpServletRequest, ArticleQuery, ArticleReviewDTO, AddCartDTO, CartVO, CartItemVO, CategoryQuery, ResponseEntity, HttpStatusCode, CraftsmanQuery, CraftsmanAuthDTO, CraftsmanReviewDTO, PasswordDTO, CraftsmanDTO, LocalDate, EmailDTO, HttpSession, EventDTO, LocalDateTime, LocalTime, EventApplyDTO, EventQuery, EventCommentAddDTO, HttpServletResponse, OrderQuery, RefundQuery, OrderReviewDTO, OrderStatsuDTO, UserSignUpRefundDTO, OrderUserVO, OrderItemUserVO, AlipayTradeFastpayRefundQueryResponse, TradeFundBill, RefundRoyaltyResult, ProductCommentLikeDTO, CommentQuery, ProductDTO, ProductListVO, ProductQuery, ProductDetailVO, QuestionQuery, QuestionRecordDTO, UserAddressDTO, UserUpdateDTO, UserRegisterDTO, WorkShopDTO, VideoVO } from './GType'

const instance: AxiosInstance = axios.create({ baseURL: import.meta.env.DEV ? '/api' : '/', timeout: 10000 })

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => { config.withCredentials = true; return config; }, (error) => Promise.reject(error));
instance.interceptors.response.use((res: AxiosResponse) => res, (err) => Promise.reject(err));

async function calling<T>(promise: Promise<AxiosResponse<T>>): Promise<T> {
    try { const res = await promise; return res.data; } catch (error: any) { throw error; }
}

// ############################################################
// # > [RestController] Admin
// ############################################################

export function Admin_page(query: PageQuery = {} as PageQuery): Promise<Result<PageBean<any>>> {
    return calling<Result<PageBean<any>>>(instance.get('/admin/page', { params: { ...query }, data: null }))
}

export function Admin_resetPassword(adminId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/admin/resetPassword', null, { params: { adminId,  } }))
}

export function Admin_changeStatus(status: string, id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/admin/status', null, { params: { status, id,  } }))
}

export function Admin_batchDelete(ids: string[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/admin/batchdelete', { params: { ids,  }, data: null }))
}

export function Admin_updateAvatar(avatar: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/admin/updateAvatar', null, { params: { avatar,  } }))
}

export function Admin_nameselect(name: string, page: string, pageSize: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/admin/nameselect', { params: { name, page, pageSize,  }, data: null }))
}

export function Admin_update(adminDTO: AdminDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/admin/update', adminDTO, { params: {  } }))
}

export function Admin_delete(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/admin/delete', { params: { id,  }, data: null }))
}

export function Admin_save(adminDTO: AdminDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/admin/save', adminDTO, { params: {  } }))
}

export function Admin_logout(): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/admin/logout', null, { params: {  } }))
}

export function Admin_login(loginDTO: LoginDTO, request: HttpServletRequest = {} as HttpServletRequest): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/admin/login', loginDTO, { params: { ...request } }))
}

// ############################################################
// # > [RestController] Article
// ############################################################

export function Article_page(articleQuery: ArticleQuery = {} as ArticleQuery): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/article/page', { params: { ...articleQuery }, data: null }))
}

export function Article_review(articleReviewDTO: ArticleReviewDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/article/review', articleReviewDTO, { params: {  } }))
}

export function Article_delete(ids: string[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/article/batch', { params: { ids,  }, data: null }))
}

export function Article_delete(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete(`/article/${id}`, { params: {  }, data: null }))
}

export function Article_setArticleStatus(dto: ArticleReviewDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/article/setArticleStatus', dto, { params: {  } }))
}

export function Article_updateCover(id: string, cover: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/article/updateCover', null, { params: { id, cover,  } }))
}

export function Article_getArticleDetail(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get(`/article/${id}`, { params: {  }, data: null }))
}

export function Article_getTop3KnowledgeArticle(): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/article/top3', { params: {  }, data: null }))
}

export function Article_pageCraftsmanArticle(query: PageQuery = {} as PageQuery): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/article/craftsman-article', { params: { ...query }, data: null }))
}

// ############################################################
// # > [RestController] Cart
// ############################################################

export function Cart_toggleCheck(dto: AddCartDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/cart/check', dto, { params: {  } }))
}

export function Cart_list(): Promise<Result<CartVO>> {
    return calling<Result<CartVO>>(instance.get('/cart/list', { params: {  }, data: null }))
}

export function Cart_delete(productIds: string[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/cart/batch', { params: { productIds,  }, data: null }))
}

// ############################################################
// # > [RestController] Category
// ############################################################

export function Category_batchDeleteCategory(ids: string[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/category/batch', { params: { ids,  }, data: null }))
}

export function Category_pageCategory(query: CategoryQuery = {} as CategoryQuery): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/category/page', { params: { ...query }, data: null }))
}

export function Category_getCategoryById(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get(`/category/${id}`, { params: {  }, data: null }))
}

export function Category_deleteCategory(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete(`/category/${id}`, { params: {  }, data: null }))
}

// ############################################################
// # > [RestController] Common
// ############################################################

export function Common_addToBloomFilter(id: any): Promise<ResponseEntity<string>> {
    return calling<ResponseEntity<string>>(instance.post(`/common/bloom-filter/add/${id}`, null, { params: {  } }))
}

export function Common_reinitBloomFilter(): Promise<ResponseEntity<string>> {
    return calling<ResponseEntity<string>>(instance.post('/common/bloom-filter/reinit', null, { params: {  } }))
}

// ############################################################
// # > [RestController] Craftsman
// ############################################################

export function Craftsman_page(craftsmanQuery: CraftsmanQuery = {} as CraftsmanQuery): Promise<Result<PageBean<any>>> {
    return calling<Result<PageBean<any>>>(instance.get('/craftsman/page', { params: { ...craftsmanQuery }, data: null }))
}

export function Craftsman_resetPassword(craftsmanId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/craftsman/resetPassword', null, { params: { craftsmanId,  } }))
}

export function Craftsman_changeStatus(status: string, id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/craftsman/status', null, { params: { status, id,  } }))
}

export function Craftsman_batchDelete(ids: string[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/craftsman/batchdelete', { params: { ids,  }, data: null }))
}

export function Craftsman_updateAvatar(avatar: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/craftsman/updateAvatar', null, { params: { avatar,  } }))
}

export function Craftsman_nameSelect(craftsmanQuery: CraftsmanQuery = {} as CraftsmanQuery): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/craftsman/nameselect', { params: { ...craftsmanQuery }, data: null }))
}

export function Craftsman_checkEmail(email: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/craftsman/check-email', { params: { email,  }, data: null }))
}

export function Craftsman_getProfile(craftsmanId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get(`/craftsman/profile/${craftsmanId}`, { params: {  }, data: null }))
}

export function Craftsman_pageReview(page: string, pageSize: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/craftsman/page-review', { params: { page, pageSize,  }, data: null }))
}

export function Craftsman_signUpAuth(craftsmanAuthDTO: CraftsmanAuthDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/craftsman/signup-auth', craftsmanAuthDTO, { params: {  } }))
}

export function Craftsman_setReviewStatus(dto: CraftsmanReviewDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/craftsman/review-status', dto, { params: {  } }))
}

export function Craftsman_updatePassword(passwordDTO: PasswordDTO, request: HttpServletRequest = {} as HttpServletRequest): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/craftsman/update-password', passwordDTO, { params: { ...request } }))
}

export function Craftsman_update(craftsmanDTO: CraftsmanDTO, request: HttpServletRequest = {} as HttpServletRequest): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/craftsman/update', craftsmanDTO, { params: { ...request } }))
}

export function Craftsman_delete(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/craftsman/delete', { params: { id,  }, data: null }))
}

export function Craftsman_save(craftsmanDTO: CraftsmanDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/craftsman/save', craftsmanDTO, { params: {  } }))
}

export function Craftsman_check(username: string, phone: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/craftsman/check', { params: { username, phone,  }, data: null }))
}

export function Craftsman_logout(): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/craftsman/logout', null, { params: {  } }))
}

export function Craftsman_login(loginDTO: LoginDTO, request: HttpServletRequest = {} as HttpServletRequest): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/craftsman/login', loginDTO, { params: { ...request } }))
}

// ############################################################
// # > [RestController] Email
// ############################################################

export function Email_sendCode(dto: EmailDTO): Promise<ResponseEntity<any>> {
    return calling<ResponseEntity<any>>(instance.post('/email/send-code', dto, { params: {  } }))
}

export function Email_verify(dto: EmailDTO, session: HttpSession = {} as HttpSession): Promise<ResponseEntity<any>> {
    return calling<ResponseEntity<any>>(instance.post('/email/verify', dto, { params: { ...session } }))
}

// ############################################################
// # > [RestController] Event
// ############################################################

export function Event_applies(eventId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get(`/event/applies/${eventId}`, { params: {  }, data: null }))
}

export function Event_update(dto: EventDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/event/update', dto, { params: {  } }))
}

export function Event_apply(dto: EventApplyDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/event/apply', dto, { params: {  } }))
}

export function Event_list(eventQuery: EventQuery = {} as EventQuery): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/event/list', { params: { ...eventQuery }, data: null }))
}

export function Event_delete(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.post(`/event/delete/${id}`, null, { params: {  } }))
}

export function Event_save(dto: EventDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/event/save', dto, { params: {  } }))
}

export function Event_comment(dto: EventCommentAddDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/event/comment', dto, { params: {  } }))
}

export function Event_sign(eventId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/event/sign', null, { params: { eventId,  } }))
}

export function Event_detail(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get(`/event/detail/${id}`, { params: {  }, data: null }))
}

// ############################################################
// # > [RestController] MinIO
// ############################################################

export function MinIO_delete(fileName: string): Promise<string> {
    return calling<string>(instance.delete(`/oss/delete/${fileName}`, { params: {  }, data: null }))
}

export function MinIO_upload(file: File | Blob): Promise<Result<any>> {
    const formData = new FormData();
    if (file !== undefined) formData.append('file', file as any);
    return calling<Result<any>>(instance.post('/oss/upload', formData))
}

export function MinIO_download(fileName: string, response: HttpServletResponse = {} as HttpServletResponse): Promise<void> {
    return calling<void>(instance.get(`/oss/download/${fileName}`, { params: { ...response }, data: null }))
}

// ############################################################
// # > [RestController] Order
// ############################################################

export function Order_page(orderQuery: OrderQuery = {} as OrderQuery): Promise<Result<PageBean<any>>> {
    return calling<Result<PageBean<any>>>(instance.get('/order/page', { params: { ...orderQuery }, data: null }))
}

export function Order_pageList(orderQuery: OrderQuery = {} as OrderQuery): Promise<Result<PageBean<any>>> {
    return calling<Result<PageBean<any>>>(instance.get('/order/admin-page', { params: { ...orderQuery }, data: null }))
}

export function Order_getOrdersCount(): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/order/craftsman-orders', { params: {  }, data: null }))
}

export function Order_refundPageList(refundQuery: RefundQuery = {} as RefundQuery): Promise<Result<PageBean<any>>> {
    return calling<Result<PageBean<any>>>(instance.get('/order/refund-page', { params: { ...refundQuery }, data: null }))
}

export function Order_reviewOrder(orderReviewDTO: OrderReviewDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/order/reviewOrder', orderReviewDTO, { params: {  } }))
}

export function Order_craftsmanPageList(orderQuery: OrderQuery = {} as OrderQuery): Promise<Result<PageBean<any>>> {
    return calling<Result<PageBean<any>>>(instance.get('/order/craftsman-page', { params: { ...orderQuery }, data: null }))
}

export function Order_updateOrderStatus(orderStatsuDTO: OrderStatsuDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/order/status', orderStatsuDTO, { params: {  } }))
}

export function Order_signUpRefund(userSignUpRefundDTO: UserSignUpRefundDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/order/refund', userSignUpRefundDTO, { params: {  } }))
}

export function Order_myOrders(): Promise<Result<OrderUserVO[]>> {
    return calling<Result<OrderUserVO[]>>(instance.get('/order/myOrders', { params: {  }, data: null }))
}

export function Order_userUpdateOrderStatus(orderStatsuDTO: OrderStatsuDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/order/status-user', orderStatsuDTO, { params: {  } }))
}

// ############################################################
// # > [RestController] Payment
// ############################################################

export function Payment_refundQuery(orderSn: string): Promise<AlipayTradeFastpayRefundQueryResponse> {
    return calling<AlipayTradeFastpayRefundQueryResponse>(instance.get('/pay/refundQuery', { params: { orderSn,  }, data: null }))
}

export function Payment_refund(orderSn: string[]): Promise<Result<string>> {
    return calling<Result<string>>(instance.post('/pay/refund', orderSn, { params: {  } }))
}

export function Payment_QRCode(orderSn: string[]): Promise<Result<string>> {
    return calling<Result<string>>(instance.post('/pay/QR', orderSn, { params: {  } }))
}

export function Payment_closeOrder(orderSn: string[]): Promise<Result<string>> {
    return calling<Result<string>>(instance.post('/pay/close', orderSn, { params: {  } }))
}

export function Payment_queryStatus(orderSn: string[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/pay/status/query', orderSn, { params: {  } }))
}

export function Payment_queryOrderIsPending(paySn: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/pay/repay', { params: { paySn,  }, data: null }))
}

// ############################################################
// # > [RestController] ProductComment
// ############################################################

export function ProductComment_deleteComment(commentId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete(`/comment/${commentId}`, { params: {  }, data: null }))
}

export function ProductComment_likeComment(productCommentLikeDTO: ProductCommentLikeDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/comment/like', productCommentLikeDTO, { params: {  } }))
}

export function ProductComment_listComments(productId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/comment/list', { params: { productId,  }, data: null }))
}

export function ProductComment_pageComments(commentQuery: CommentQuery = {} as CommentQuery): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/comment/page', { params: { ...commentQuery }, data: null }))
}

// ############################################################
// # > [RestController] Product
// ############################################################

export function Product_batchDelete(ids: string[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/product/batch', { params: {  }, data: ids }))
}

export function Product_review(reviewDTO: ProductDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/product/review', reviewDTO, { params: {  } }))
}

export function Product_myOffline(productId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/product/my/productStatus', null, { params: { productId,  } }))
}

export function Product_myFavorite(productId: string): Promise<Result<ProductListVO[]>> {
    return calling<Result<ProductListVO[]>>(instance.get('/product/myFavorite', { params: { productId,  }, data: null }))
}

export function Product_adminPage(query: ProductQuery = {} as ProductQuery): Promise<Result<PageBean<any>>> {
    return calling<Result<PageBean<any>>>(instance.get('/product/page', { params: { ...query }, data: null }))
}

export function Product_myPage(query: ProductQuery = {} as ProductQuery): Promise<Result<PageBean<any>>> {
    return calling<Result<PageBean<any>>>(instance.get('/product/my/page', { params: { ...query }, data: null }))
}

export function Product_offline(productId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/product/offline', null, { params: { productId,  } }))
}

export function Product_upline(productId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/product/upline', null, { params: { productId,  } }))
}

export function Product_like(productId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.post(`/product/like/${productId}`, null, { params: {  } }))
}

export function Product_myLike(productId: string): Promise<Result<ProductListVO[]>> {
    return calling<Result<ProductListVO[]>>(instance.get('/product/myLike', { params: { productId,  }, data: null }))
}

export function Product_recommend(size: string): Promise<Result<ProductListVO[]>> {
    return calling<Result<ProductListVO[]>>(instance.get('/product/recommend', { params: { size,  }, data: null }))
}

export function Product_byCraftsman(craftsmanId: string): Promise<Result<ProductListVO[]>> {
    return calling<Result<ProductListVO[]>>(instance.get(`/product/by-craftsman/${craftsmanId}`, { params: {  }, data: null }))
}

export function Product_update(dto: ProductDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/product/update', dto, { params: {  } }))
}

export function Product_collect(productId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.post(`/product/favorite/${productId}`, null, { params: {  } }))
}

export function Product_list(search: ProductQuery = {} as ProductQuery): Promise<Result<PageBean<ProductListVO>>> {
    return calling<Result<PageBean<ProductListVO>>>(instance.get('/product/list', { params: { ...search }, data: null }))
}

export function Product_delete(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete(`/product/${id}`, { params: {  }, data: null }))
}

export function Product_save(dto: ProductDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/product/save', dto, { params: {  } }))
}

export function Product_detail(id: string): Promise<Result<ProductDetailVO>> {
    return calling<Result<ProductDetailVO>>(instance.get(`/product/detail/${id}`, { params: {  }, data: null }))
}

// ############################################################
// # > [RestController] Question
// ############################################################

export function Question_page(query: QuestionQuery = {} as QuestionQuery): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/question/page', { params: { ...query }, data: null }))
}

export function Question_submitQuiz(records: QuestionRecordDTO[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/question/submit', records, { params: {  } }))
}

export function Question_startQuiz(count: string, categoryId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/question/start', { params: { count, categoryId,  }, data: null }))
}

export function Question_deleteBatch(ids: string[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/question/batch', { params: {  }, data: ids }))
}

export function Question_delete(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete(`/question/${id}`, { params: {  }, data: null }))
}

// ############################################################
// # > [RestController] User
// ############################################################

export function User_page(page: string, pageSize: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/user/page', { params: { page, pageSize,  }, data: null }))
}

export function User_updateAvatar(avatar: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/user/updateAvatar', null, { params: { avatar,  } }))
}

export function User_addAddress(addressDTO: UserAddressDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/user/address/add', addressDTO, { params: {  } }))
}

export function User_checkEmail(email: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/user/check-email', { params: { email,  }, data: null }))
}

export function User_userDelete(userId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete(`/user/remove/${userId}`, { params: {  }, data: null }))
}

export function User_updatePassword(passwordDTO: PasswordDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/user/password-update', passwordDTO, { params: {  } }))
}

export function User_getUserByNickName(nickname: string, page: string, pageSize: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/user/nameselect', { params: { nickname, page, pageSize,  }, data: null }))
}

export function User_batchDeleteAddress(addressIds: string[]): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete('/user/address/batch', { params: {  }, data: addressIds }))
}

export function User_setDefaultAddress(addressId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/user/address/setDefault', null, { params: { addressId,  } }))
}

export function User_getAddressList(): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/user/address', { params: {  }, data: null }))
}

export function User_updateAddress(addressDTO: UserAddressDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/user/address/update', addressDTO, { params: {  } }))
}

export function User_deleteAddress(addressId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.delete(`/user/address/delete/${id}`, { params: {  }, data: null }))
}

export function User_getUserProfile(): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/user/profile', { params: {  }, data: null }))
}

export function User_update(userUpdateDTO: UserUpdateDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/user/update', userUpdateDTO, { params: {  } }))
}

export function User_save(userRegisterDTO: UserRegisterDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/user/register', userRegisterDTO, { params: {  } }))
}

export function User_check(username: string, phone: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/user/check', { params: { username, phone,  }, data: null }))
}

export function User_updateStatus(userId: string, status: string, rejectReason: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/user/status', null, { params: { userId, status, rejectReason,  } }))
}

export function User_logout(): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/user/logout', null, { params: {  } }))
}

export function User_login(loginDTO: LoginDTO, request: HttpServletRequest = {} as HttpServletRequest): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/user/login', loginDTO, { params: { ...request } }))
}

// ############################################################
// # > [RestController] WorkShop
// ############################################################

export function WorkShop_page(query: PageQuery = {} as PageQuery): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/workshop/page', { params: { ...query }, data: null }))
}

export function WorkShop_getWorkShopByCraftsmanId(): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/workshop/get-workshop', { params: {  }, data: null }))
}

export function WorkShop_banWorkshop(workshopId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/workshop/ban-workshop', null, { params: { workshopId,  } }))
}

export function WorkShop_reviewWorkshop(workshopId: string, status: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/workshop/review-workshop', null, { params: { workshopId, status,  } }))
}

export function WorkShop_collectWorkShop(workShopId: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.post(`/workshop/collect/${workShopId}`, null, { params: {  } }))
}

export function WorkShop_visitWorkShop(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/workshop/visit', null, { params: { id,  } }))
}

export function WorkShop_getShopStatus(): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/workshop/getShopStatus', { params: {  }, data: null }))
}

export function WorkShop_signUpWorkShop(workShopDTO: WorkShopDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.post('/workshop/sign-up', workShopDTO, { params: {  } }))
}

export function WorkShop_updateWorkShop(workShopDTO: WorkShopDTO): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/workshop/update', workShopDTO, { params: {  } }))
}

export function WorkShop_selectWorkShopName(workshopName: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get('/workshop/nameselect', { params: { workshopName,  }, data: null }))
}

export function WorkShop_working(status: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.put('/workshop/set-workshopStatus', null, { params: { status,  } }))
}

export function WorkShop_detail(id: string): Promise<Result<any>> {
    return calling<Result<any>>(instance.get(`/workshop/detail/${id}`, { params: {  }, data: null }))
}

