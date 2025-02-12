package com.example.Backend.controller.member;

import com.example.Backend.controller.member.form.CheckPasswordForm;
import com.example.Backend.controller.member.form.MemberLoginForm;
import com.example.Backend.controller.member.form.MemberRegisterForm;
import com.example.Backend.controller.member.form.PasswordUpdateForm;
import com.example.Backend.entity.member.Member;
import com.example.Backend.service.member.MemberService;
import com.example.Backend.service.member.response.MemberResponse;
import com.example.Backend.service.security.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

import static com.example.Backend.controller.order.OrderController.getaLong;


@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    final private MemberService memberService;
    final private RedisService redisService;

    @PostMapping("/check-email/{email}")
    public Boolean emailValidation(@PathVariable("email") String email) {
        log.info("emailValidation(): " + email);

        return memberService.emailValidation(email);
    }

    @PostMapping("/sign-up")
    public Boolean signUp(@RequestBody MemberRegisterForm form) {
        log.info("signUp(): " + form);

        return memberService.signUp(form.toMemberRegisterRequest());
    }

    @PostMapping("/check-manager/{managerCode}")
    public Boolean managerCodeValidation(@PathVariable("managerCode") String managerCode) {
        log.info("managerCodeValidation(): " + managerCode);

        return memberService.managerCodeValidation(managerCode);
    }

    @PostMapping("/sign-in")
    public String signIn(@RequestBody MemberLoginForm form) {
        log.info("signIn(): " + form);

        return memberService.signIn(form.toMemberLoginRequest());
    }

    @PostMapping("/passwordCheck")
    public Boolean passwordCheck(@RequestBody CheckPasswordForm checkPasswordForm) {
        return memberService.passwordCheck(checkPasswordForm);
    }

    @PutMapping("/passwordUpdate")
    public Boolean passwordUpdate(@RequestBody PasswordUpdateForm passwordUpdateForm) {
       log.info("PasswordUpdate : " + passwordUpdateForm );

       return memberService.passwordUpdate(passwordUpdateForm);
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody String token) {
        try {
            token = token.substring(0, token.length() - 1);
            log.info("logout(): " + token);

            redisService.deleteByKey(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error during logout: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/account")
    public MemberResponse account(@RequestBody String token) {
        token = token.substring(0, token.length() - 1);
        log.info("logout(): " + token);
        Long memberId = null;
        String memberValue = redisService.getValueByKey(token);
        if (memberValue != null) {
            String[] value = memberValue.split(":");
            if (value.length > 0) {
                memberId = Long.valueOf(value[0]);
            }
        }
        return memberService.read(memberId);
    }

    private Long getMemberIdByToken(String token) {
        return getaLong(token, log, redisService);
    }

    @DeleteMapping("/delete")
    public void delete(@RequestBody String token) {
        token = token.substring(0, token.length() - 1);
        log.info("logout(): " + token);
        Long memberId = null;
        String memberValue = redisService.getValueByKey(token);
        if (memberValue != null) {
            String[] value = memberValue.split(":");
            if (value.length > 0) {
                memberId = Long.valueOf(value[0]);
            }
        }
        memberService.delete(memberId);
    }

    @PostMapping("/ismanager")
    public boolean isManager(@RequestBody String token) {
        token = token.substring(0, token.length() - 1);
        log.info("logout(): " + token);
        String authorityName = null;
        String memberValue = redisService.getValueByKey(token);
        if (memberValue != null) {
            String[] value = memberValue.split(":");
            if (value.length > 0) {
                authorityName = value[1];
                log.info("authorityName: " + authorityName);
            }
        }
        return authorityName.equals("MANAGER");
    }

}