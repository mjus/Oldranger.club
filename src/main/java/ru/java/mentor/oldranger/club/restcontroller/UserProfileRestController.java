package ru.java.mentor.oldranger.club.restcontroller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.java.mentor.oldranger.club.dto.*;
import ru.java.mentor.oldranger.club.model.forum.Topic;
import ru.java.mentor.oldranger.club.model.forum.TopicVisitAndSubscription;
import ru.java.mentor.oldranger.club.model.mail.Direction;
import ru.java.mentor.oldranger.club.model.user.User;
import ru.java.mentor.oldranger.club.model.user.UserProfile;
import ru.java.mentor.oldranger.club.model.user.UserStatistic;
import ru.java.mentor.oldranger.club.service.forum.CommentService;
import ru.java.mentor.oldranger.club.service.forum.TopicService;
import ru.java.mentor.oldranger.club.service.forum.TopicVisitAndSubscriptionService;
import ru.java.mentor.oldranger.club.service.mail.MailDirectionService;
import ru.java.mentor.oldranger.club.service.user.*;
import ru.java.mentor.oldranger.club.service.utils.SecurityUtilsService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
@Tag(name = "User profile")
public class UserProfileRestController {

    private UserProfileService userProfileService;
    private UserStatisticService userStatisticService;
    private UserService userService;
    private InvitationService invitationService;
    private TopicService topicService;
    private TopicVisitAndSubscriptionService topicVisitAndSubscriptionService;
    private CommentService commentService;
    private PasswordEncoder passwordEncoder;
    private SecurityUtilsService securityUtilsService;
    private MailDirectionService mailDirectionService;


    @InitBinder
    // передаем пустые строки как null
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @Operation(security = @SecurityRequirement(name = "security"),
               summary = "Get ProfileDto of logged in user", tags = { "User profile" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = ProfileDto.class))),
            @ApiResponse(responseCode = "204", description = "User is not logged in")})
    @GetMapping(value = "/profile", produces = { "application/json" })
    public ResponseEntity<ProfileDto> getProfile() {

        User user = securityUtilsService.getLoggedUser();
        if (user == null) return ResponseEntity.noContent().build();

        UserProfile profile = userProfileService.getUserProfileByUser(user);
        UserStatistic stat = userStatisticService.getUserStaticByUser(user);
        ProfileDto dto = userProfileService.buildProfileDto(profile, stat, true);
        return ResponseEntity.ok(dto);
    }

    @Operation(security = @SecurityRequirement(name = "security"),
               summary = "Get ProfileDto of another user by id", tags = { "User profile" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = ProfileDto.class))),
            @ApiResponse(responseCode = "204", description = "User by id not found")})
    @GetMapping(value = "/{id}", produces = { "application/json" })
    public ResponseEntity<ProfileDto> getAnotherUserProfile(@PathVariable Long id) {
        User user;
        try {
            user = userService.findById(id);
        } catch (Exception e){
            return ResponseEntity.notFound().build();
        }
        UserProfile profile = userProfileService.getUserProfileByUser(user);
        UserStatistic stat = userStatisticService.getUserStaticByUser(user);
        ProfileDto dto = userProfileService.buildProfileDto(profile, stat, false);
        return ResponseEntity.ok(dto);
    }

    @Operation(security = @SecurityRequirement(name = "security"),
               summary = "Update profile", tags = { "User profile" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = UpdateProfileDto.class))),
            @ApiResponse(responseCode = "204", description = "User is not logged in")})
    @PostMapping("/updateProfile")
    public ResponseEntity<UpdateProfileDto> updateProfile(UserProfile profile) {

        User currentUser = securityUtilsService.getLoggedUser();
        if (currentUser == null) return ResponseEntity.noContent().build();

        if (profile.getUser().getNickName() == null || profile.getUser().getEmail() == null){
            UpdateProfileDto dto = new UpdateProfileDto(profile, new ErrorDto("Поля 'Ник' и 'Email' обязательно должны быть заполнены"));
            return ResponseEntity.ok(dto);
        }
        User user = userService.getUserByNickName(profile.getUser().getNickName());
        if (user != null && !currentUser.getNickName().equals(user.getNickName())) {
            UpdateProfileDto dto = new UpdateProfileDto(profile, new ErrorDto("Пользователь с таким ником уже существует"));
            return ResponseEntity.ok(dto);
        }
        user = userService.getUserByEmail(profile.getUser().getEmail());
        if (user != null && !currentUser.getEmail().equals(user.getEmail())) {
            UpdateProfileDto dto = new UpdateProfileDto(profile, new ErrorDto("Пользователь с таким адресом почты уже существует"));
            return ResponseEntity.ok(dto);
        }
        currentUser.setNickName(profile.getUser().getNickName());
        currentUser.setFirstName(profile.getUser().getFirstName());
        currentUser.setLastName(profile.getUser().getLastName());
        currentUser.setEmail(profile.getUser().getEmail());
        userService.save(currentUser);
        profile.setUser(currentUser);
        userProfileService.editUserProfile(profile);
        UpdateProfileDto dto = new UpdateProfileDto(profile, null);
        return ResponseEntity.ok(dto);
    }

    @Operation(security = @SecurityRequirement(name = "security"),
               summary = "Get CommentDto list for current user", tags = { "User profile" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "204", description = "User is not logged in")})
    @GetMapping(value = "/comments", produces = { "application/json" })
    public ResponseEntity<List<CommentDto>> getComments(
                                            @RequestAttribute(value = "page", required = false) Integer page) {

        if (page == null) page = 0;
        Pageable pageable = PageRequest.of(page, 10, Sort.by("dateTime").descending());

        User currentUser = securityUtilsService.getLoggedUser();
        if (currentUser == null) return ResponseEntity.noContent().build();

        List<CommentDto> dtos = commentService.getPageableCommentDtoByUser(currentUser, pageable).getContent();
        return ResponseEntity.ok(dtos);
    }

    @Operation(security = @SecurityRequirement(name = "security"),
               summary = "Get TopicVisitAndSubscription list for current user", tags = { "User profile" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = TopicVisitAndSubscription.class))),
            @ApiResponse(responseCode = "204", description = "User is not logged in")})
    @GetMapping(value = "/subscriptions", produces = { "application/json" })
    public ResponseEntity<List<TopicVisitAndSubscription>> getSubscriptions(
                                   @RequestAttribute(value = "page", required = false) Integer page,
                                   @Parameter(description="Not required, by default size: 10")
                                   @PageableDefault(size = 10) Pageable pageable) {
        User currentUser = securityUtilsService.getLoggedUser();
        if (currentUser == null) return ResponseEntity.noContent().build();

        if (page != null) {
            pageable = PageRequest.of(page, 10, Sort.by("lastMessageTime"));
        }

        List<TopicVisitAndSubscription> dtos = topicVisitAndSubscriptionService.getPagebleTopicVisitAndSubscriptionForUser(currentUser,pageable).getContent();
        return ResponseEntity.ok(dtos);
    }

    @Operation(security = @SecurityRequirement(name = "security"),
               summary = "Get all topics started by current user", tags = { "User profile" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = Topic.class))),
            @ApiResponse(responseCode = "204", description = "User is not logged in")})
    @GetMapping(value = "/topics", produces = { "application/json" })
    public ResponseEntity<List<Topic>> getTopics(@RequestParam(value = "page", required = false) Integer page) {

        User currentUser = securityUtilsService.getLoggedUser();
        if (currentUser == null) return ResponseEntity.noContent().build();

        if (page == null) page = 0;
        Pageable pageable = PageRequest.of(page, 10, Sort.by("lastMessageTime"));

        List<Topic> dtos = topicService.findAllTopicsStartedByUser(currentUser,pageable).getContent();
        return ResponseEntity.ok(dtos);
    }

    @Operation(security = @SecurityRequirement(name = "security"),
               summary = "Get InviteDto for current user", tags = { "User profile" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = InviteDto.class))),
            @ApiResponse(responseCode = "204", description = "User is not logged in")})
    @GetMapping(value = "/invite", produces = { "application/json" })
    public ResponseEntity<InviteDto> getInvitation() {
        User currentUser = securityUtilsService.getLoggedUser();
        if (currentUser == null) return ResponseEntity.noContent().build();

        String key = invitationService.getCurrentKey(currentUser);
        InviteDto dto = new InviteDto(currentUser, key);
        return ResponseEntity.ok(dto);
    }

    @Operation(security = @SecurityRequirement(name = "security"),
               summary = "Change password", tags = { "User profile" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))),
            @ApiResponse(responseCode = "204", description = "User is not logged in")})
    @PostMapping(value = "/changePassword", produces = { "application/json" })
    public ResponseEntity<ErrorDto> changePassword(@RequestParam String oldPass,
                                                   @RequestParam String newPass,
                                                   @RequestParam String passConfirm) {
        User currentUser = securityUtilsService.getLoggedUser();
        if (currentUser == null) return ResponseEntity.noContent().build();

        if (passwordEncoder.matches(oldPass,currentUser.getPassword())){
            if (passConfirm.equals(newPass)){
                currentUser.setPassword(passwordEncoder.encode(newPass));
                userService.save(currentUser);
                return ResponseEntity.ok(new ErrorDto("Пароль обновлен"));
            }
        }
        return ResponseEntity.ok(new ErrorDto("Пароль указан неверно"));
    }


    @PostMapping(value = "/changeUserDirection")
    public void setUserDirection(@RequestParam Long userId,
                                 @RequestParam String userDirection){
        mailDirectionService.changeUserDirection(userId, Direction.stringToDirectionType(userDirection));
    }
    @Operation(security = @SecurityRequirement(name = "security"),
            summary = "Get ID of currently logged user", tags = { "User profile" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "204", description = "User is not logged in")})
    @GetMapping(value = "/getloggeduserid", produces = { "application/json" })
    public ResponseEntity<Long> getCurrentUserId() {
        User currentUser = securityUtilsService.getLoggedUser();
        if (currentUser == null) return ResponseEntity.noContent().build();

        return ResponseEntity.ok(currentUser.getId());
    }

}
