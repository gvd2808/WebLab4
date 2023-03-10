package ru.combyte.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.ContentCachingRequestWrapper;
import ru.combyte.Utils;
import ru.combyte.controller.utils.LoginControllerUtils;
import ru.combyte.dao.login.LoginSystemDAO;
import ru.combyte.enitities.json.received.UserJson;
import ru.combyte.enitities.json.sended.answers.LoginState;
import ru.combyte.enitities.json.sended.errors.ErrorType;

import java.io.IOException;
import java.util.Optional;

import static ru.combyte.controller.utils.LoginControllerUtils.getWrongCharactersValues;
import static ru.combyte.controller.utils.LoginControllerUtils.getWrongLengthValues;
import static ru.combyte.enitities.json.sended.answers.LoginState.State.*;
import static ru.combyte.enitities.json.sended.errors.ErrorType.Type.*;

public class LoginFilter implements Filter {
    public LoginSystemDAO loginSystemDAO;
    public ObjectMapper objectMapper;

    public LoginFilter(LoginSystemDAO loginSystemDAO, ObjectMapper objectMapper) {
        this.loginSystemDAO = loginSystemDAO;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        var httpResponse = (HttpServletResponse) response;
        var wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        wrappedRequest.getInputStream(); // this line is necessary to cache InputStream
        UserJson user;
        try {
            user = objectMapper.readValue(wrappedRequest.getInputStream(), UserJson.class);
        } catch (JsonProcessingException e) {
            Utils.writeJsonContent(httpResponse, getBrokenJsonStructureErrorAsJson());
            httpResponse.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return;
        }
        Optional<String> badUserJsonValueAsErrorJson = getBadUserJsonValueAsErrorJson(user);
        if (badUserJsonValueAsErrorJson.isPresent()) {
            Utils.writeJsonContent(httpResponse, badUserJsonValueAsErrorJson.get());
            httpResponse.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
            return;
        }
        Optional<String> badUserBusinessValuesAsExceptionJson = getBadUserBusinessValuesAsExceptionJson(user);
        if (badUserBusinessValuesAsExceptionJson.isPresent()) {
            Utils.writeJsonContent(httpResponse, badUserBusinessValuesAsExceptionJson.get());
            httpResponse.setStatus(HttpStatus.OK.value());
            return;
        }
        Optional<String> badBusinessLoginStateAsJsonException = getBadBusinessLoginStateAsExceptionJson(user);
        if (badBusinessLoginStateAsJsonException.isPresent()) {
            Utils.writeJsonContent(httpResponse, badBusinessLoginStateAsJsonException.get());
            httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }
        chain.doFilter(wrappedRequest, httpResponse);
    }

    @SneakyThrows
    private String getBrokenJsonStructureErrorAsJson() {
        return objectMapper.writeValueAsString(new ErrorType(WRONG_JSON_STRUCTURE));
    }


    /**
     * @return null if logon
     */
    @SneakyThrows
    private Optional<String> getBadBusinessLoginStateAsExceptionJson(@NonNull UserJson user) {
        if (!loginSystemDAO.isLoginPresented(user.getLogin())) {
            return getWrongLoginAsErrorAsJsonError();
        }
        if (!loginSystemDAO.isUserPresented(user.getLogin(), user.getPassword())) {
            return getWrongPasswordAsErrorAsJsonError();
        }
        return Optional.empty();
    }

    @SneakyThrows
    private Optional<String> getWrongLoginAsErrorAsJsonError() {
        var loginState = new LoginState(WRONG_LOGIN);
        return getLoginStateAsOptionalJson(loginState);
    }

    @SneakyThrows
    private Optional<String> getWrongPasswordAsErrorAsJsonError() {
        var loginState = new LoginState(WRONG_PASSWORD);
        return getLoginStateAsOptionalJson(loginState);
    }

    private Optional<String> getLoginStateAsOptionalJson(@NonNull LoginState loginState) throws JsonProcessingException {
        return Optional.of(objectMapper.writeValueAsString(loginState));
    }

    /**
     * @return None if user format is bad
     */
    private Optional<String> getBadUserJsonValueAsErrorJson(@NonNull UserJson user) {
        var absentKeysError = getAbsentKeysErrorAsErrorJson(user);
        if (absentKeysError.isPresent()) {
            return absentKeysError;
        }
        return Optional.empty();
    }

    /**
     * @return None if user format is bad
     */
    private Optional<String> getBadUserBusinessValuesAsExceptionJson(@NonNull UserJson user) {
        var wrongLengthValuesException = getWrongLengthValuesAsExceptionJson(user);
        if (wrongLengthValuesException.isPresent()) {
            return wrongLengthValuesException;
        }
        var wrongCharactersValuesException = getWrongCharactersValuesAsExceptionJson(user);
        if (wrongCharactersValuesException.isPresent()) {
            return wrongCharactersValuesException;
        }
        return Optional.empty();
    }

    /**
     * @return None if hasn't absent keys
     */
    @SneakyThrows
    private Optional<String> getAbsentKeysErrorAsErrorJson(@NonNull UserJson user) {
        var loginCommandStructureMissingKeys = LoginControllerUtils.getCommandWithFullUserMissingKeys(user);
        if (!loginCommandStructureMissingKeys.isEmpty()) {
            var errorType = new ErrorType(ABSENT_KEY, loginCommandStructureMissingKeys);
            return Optional.of(objectMapper.writeValueAsString(errorType));
        }
        return Optional.empty();
    }

    /**
     * @return None if hasn't wrong values
     */
    @SneakyThrows
    private Optional<String> getWrongCharactersValuesAsExceptionJson(@NonNull UserJson user) {
        var wrongCharactersValues = getWrongCharactersValues(user);
        if (!wrongCharactersValues.isEmpty()) {
            var loginState = new LoginState(WRONG_CHARACTER, wrongCharactersValues);
            return Optional.of(objectMapper.writeValueAsString(loginState));
        }
        return Optional.empty();
    }

    /**
     * @return None if hasn't wrong values
     */
    @SneakyThrows
    private Optional<String> getWrongLengthValuesAsExceptionJson(@NonNull UserJson user) {
        var wrongLengthValues = getWrongLengthValues(user);
        if (!wrongLengthValues.isEmpty()) {
            var loginState = new LoginState(WRONG_LENGTH, wrongLengthValues);
            return Optional.of(objectMapper.writeValueAsString(loginState));
        }
        return Optional.empty();
    }
}












