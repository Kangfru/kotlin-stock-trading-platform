package com.kangfru.domain.account.controller

import com.kangfru.domain.account.dto.AccountResponse
import com.kangfru.domain.account.dto.CreateAccountRequest
import com.kangfru.domain.account.service.AccountService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping
    fun createAccount(@RequestBody @Valid request: CreateAccountRequest): ResponseEntity<AccountResponse> {
        val response = accountService.createAccount(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{accountId}")
    fun getAccount(@PathVariable accountId: Long): ResponseEntity<AccountResponse> {
        val response = accountService.getAccount(accountId)
        return ResponseEntity.ok(response)
    }

}