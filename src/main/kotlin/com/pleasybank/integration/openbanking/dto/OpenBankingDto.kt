package com.pleasybank.integration.openbanking.dto

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 오픈뱅킹 API 관련 DTO 클래스들
 */

// 계좌 조회 관련 DTO
data class AccountListRequest(
    val user_seq_no: String,
    val include_cancel_yn: String = "N",
    val sort_order: String = "D"
)

data class AccountListResponse(
    val api_tran_id: String,
    val api_tran_dtm: String,
    val rsp_code: String,
    val rsp_message: String,
    val user_name: String,
    val res_cnt: Int,
    val res_list: List<AccountDto>
)

data class AccountDto(
    val fintech_use_num: String,
    val account_alias: String,
    val bank_code_std: String,
    val bank_code_sub: String,
    val bank_name: String,
    val account_num_masked: String,
    val account_holder_name: String,
    val account_type: String,
    val inquiry_agree_yn: String,
    val inquiry_agree_dtime: String,
    val transfer_agree_yn: String,
    val transfer_agree_dtime: String,
    val account_state: String
)

// 잔액 조회 관련 DTO
data class AccountBalanceRequest(
    val bank_tran_id: String,
    val fintech_use_num: String,
    val tran_dtime: String
)

data class AccountBalanceResponse(
    val api_tran_id: String,
    val api_tran_dtm: String,
    val rsp_code: String,
    val rsp_message: String,
    val bank_tran_id: String,
    val bank_tran_date: String,
    val bank_code_tran: String,
    val bank_rsp_code: String,
    val bank_rsp_message: String,
    val fintech_use_num: String,
    val balance_amt: String,
    val available_amt: String,
    val account_type: String,
    val product_name: String,
    val account_issue_date: String,
    val maturity_date: String,
    val last_tran_date: String
)

// 이체 관련 DTO
data class TransferRequest(
    val bank_tran_id: String,
    val cntr_account_type: String,
    val cntr_account_num: String,
    val dps_print_content: String,
    val fintech_use_num: String,
    val tran_amt: String,
    val tran_dtime: String,
    val req_client_name: String,
    val req_client_fintech_use_num: String,
    val req_client_num: String,
    val transfer_purpose: String,
    val recv_client_name: String,
    val recv_client_bank_code: String,
    val recv_client_account_num: String
)

data class TransferResponse(
    val api_tran_id: String,
    val api_tran_dtm: String,
    val rsp_code: String,
    val rsp_message: String,
    val dps_bank_code_std: String,
    val dps_bank_code_sub: String,
    val dps_bank_name: String,
    val dps_account_num_masked: String,
    val dps_print_content: String,
    val dps_account_holder_name: String,
    val bank_tran_id: String,
    val bank_tran_date: String,
    val bank_code_tran: String,
    val bank_rsp_code: String,
    val bank_rsp_message: String,
    val fintech_use_num: String,
    val account_alias: String,
    val bank_code_std: String,
    val bank_code_sub: String,
    val bank_name: String,
    val account_num_masked: String,
    val print_content: String,
    val account_holder_name: String,
    val tran_amt: String,
    val wd_limit_remain_amt: String
)

// 거래내역 조회 관련 DTO
data class TransactionListRequest(
    val bank_tran_id: String,
    val fintech_use_num: String,
    val inquiry_type: String,
    val inquiry_base: String,
    val from_date: String,
    val to_date: String,
    val sort_order: String,
    val tran_dtime: String
)

data class TransactionListResponse(
    val api_tran_id: String,
    val api_tran_dtm: String,
    val rsp_code: String,
    val rsp_message: String,
    val bank_tran_id: String,
    val bank_tran_date: String,
    val bank_code_tran: String,
    val bank_rsp_code: String,
    val bank_rsp_message: String,
    val fintech_use_num: String,
    val page_record_cnt: Int,
    val next_page_yn: String,
    val befor_inquiry_trace_info: String,
    val res_cnt: Int,
    val res_list: List<TransactionDto>
)

data class TransactionDto(
    val tran_date: String,
    val tran_time: String,
    val inout_type: String,
    val tran_type: String,
    val print_content: String,
    val tran_amt: String,
    val after_balance_amt: String,
    val branch_name: String
)

// 인증 토큰 관련 DTO
data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val scope: String,
    val user_seq_no: String
) 