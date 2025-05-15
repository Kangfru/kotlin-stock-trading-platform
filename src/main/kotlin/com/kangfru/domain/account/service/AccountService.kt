package com.kangfru.domain.account.service

import com.kangfru.domain.account.dto.AccountResponse
import com.kangfru.domain.account.dto.CreateAccountRequest
import com.kangfru.domain.account.model.Account
import com.kangfru.domain.account.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {

    @Transactional
    fun createAccount(request: CreateAccountRequest): AccountResponse {
        val account = Account(
            userId = request.userId,
            balance = request.initialBalance
        )

        return accountRepository.save(account).toResponse()
    }

    @Transactional(readOnly = true)
    fun getAccount(accountId: Long): AccountResponse {
        return accountRepository.findById(accountId)
            .orElseThrow { IllegalArgumentException("계좌를 찾을 수 없습니다.") }
            .toResponse()
    }

    private fun Account.toResponse() = AccountResponse(
        id = id!!,
        userId = userId,
        balance = balance
    )

}