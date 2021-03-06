package com.lyubenblagoev.postfixrest.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.service.AccountService;
import com.lyubenblagoev.postfixrest.service.BccService;
import com.lyubenblagoev.postfixrest.service.model.AccountResource;
import com.lyubenblagoev.postfixrest.service.model.BccResource;

@RestController
@RequestMapping("/api/v1/domains/{domain}/accounts/{account}/bccs")
public class BccController {

	private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found";
	private static final String BCC_NOT_FOUND_MESSAGE = "BCC not found";
	private final BccService bccService;
	private final AccountService accountService;
	
	public BccController(BccService bccService, AccountService accountService) {
		this.bccService = bccService;
		this.accountService = accountService;
	}
	
	@GetMapping(value = "/outgoing")
	public ResponseEntity<BccResource> getOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		return bccService.getOutgoingBcc(domain, account)
				.map(bcc -> ResponseEntity.ok().body(bcc))
				.orElseThrow(() -> new NotFoundException(BCC_NOT_FOUND_MESSAGE));
	}

	@GetMapping(value = "/incomming")
	public ResponseEntity<BccResource> getIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		return bccService.getIncommingBcc(domain, account)
				.map(bcc -> ResponseEntity.ok().body(bcc))
				.orElseThrow(() -> new NotFoundException(BCC_NOT_FOUND_MESSAGE));
	}

	@PostMapping(value = "/outgoing")
	public ResponseEntity<BccResource> addOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		Optional<AccountResource> existingAccount = accountService.getAccountByNameAndDomainName(account, domain);
		return existingAccount.map(a -> {
			bcc.setAccountId(a.getId());
			return bccService.saveOutgoingBcc(bcc)
					.map(saved -> ResponseEntity.ok().body(saved))
					.orElseThrow(() -> new NotFoundException(BCC_NOT_FOUND_MESSAGE));
		}).orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
	}

	@PostMapping(value = "/incomming")
	public ResponseEntity<BccResource> addIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		Optional<AccountResource> existingAccount = accountService.getAccountByNameAndDomainName(account, domain);
		return existingAccount.map(a -> {
			bcc.setAccountId(a.getId());
			return bccService.saveIncommingBcc(bcc)
					.map(saved -> ResponseEntity.ok().body(saved))
					.orElseThrow(() -> new NotFoundException(BCC_NOT_FOUND_MESSAGE));
		}).orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
	}

	@PutMapping(value = "/outgoing")
	public ResponseEntity<BccResource> editOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		Optional<AccountResource> accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		return accountResource.map(a -> {
			bcc.setAccountId(a.getId());
			return bccService.getOutgoingBcc(domain, account)
					.map(existingBcc-> {
						bcc.setId(existingBcc.getId());
						if (bcc.getEmail() == null) {
							bcc.setEmail(existingBcc.getEmail());
						}
						return bccService.saveOutgoingBcc(bcc)
								.map(saved -> ResponseEntity.ok().body(saved))
								.orElse(ResponseEntity.notFound().build());
					})
					.orElseThrow(() -> new NotFoundException(BCC_NOT_FOUND_MESSAGE));
		}).orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
	}

	@PutMapping(value = "/incomming")
	public ResponseEntity<BccResource> editIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account, 
			@Validated @RequestBody BccResource bcc, BindingResult result) {
		checkForErrors(result);
		Optional<AccountResource> accountResource = accountService.getAccountByNameAndDomainName(account, domain);
		return accountResource.map(a -> {
			bcc.setAccountId(a.getId());
			return bccService.getIncommingBcc(domain, account)
					.map(existingBcc-> {
						bcc.setId(existingBcc.getId());
						if (bcc.getEmail() == null) {
							bcc.setEmail(existingBcc.getEmail());
						}
						return bccService.saveIncommingBcc(bcc)
								.map(saved -> ResponseEntity.ok().body(saved))
								.orElse(ResponseEntity.notFound().build());
					})
					.orElse(ResponseEntity.notFound().build());
		}).orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
	}

	@DeleteMapping(value = "/outgoing")
	public ResponseEntity<?> deleteOutgoingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		return bccService.getOutgoingBcc(domain, account)
				.map(bcc -> {
					bccService.deleteOutgoingBcc(bcc);
					return ResponseEntity.ok().build();
				})
				.orElseThrow(() -> new NotFoundException(BCC_NOT_FOUND_MESSAGE));
	}

	@DeleteMapping(value = "/incomming")
	public ResponseEntity<?> deleteIncommingBcc(@PathVariable("domain") String domain, @PathVariable("account") String account) {
		return bccService.getOutgoingBcc(domain, account)
				.map(bcc -> {
					bccService.deleteIncommingBcc(bcc);
					return ResponseEntity.ok().build();
				})
				.orElseThrow(() -> new NotFoundException(BCC_NOT_FOUND_MESSAGE));
	}

	private void checkForErrors(BindingResult result) {
		if (result.hasErrors()) {
			throw new BadRequestException(ControllerUtils.getError(result));
		}
	}

}