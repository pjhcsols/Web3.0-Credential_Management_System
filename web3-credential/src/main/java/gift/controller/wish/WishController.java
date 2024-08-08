package gift.controller.wish;

import gift.domain.user.User;
import gift.domain.wish.Wish;
import gift.domain.wish.WishRequest;
import gift.domain.wish.WishResponse;
import gift.service.wish.WishService;
import gift.validation.LoginMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/wishes")
public class WishController {
    private final WishService wishService;

    @Autowired
    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    @Operation(summary = "위시리스트에 항목 추가", description = "로그인한 사용자의 위시리스트에 새로운 항목을 추가합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveWish(
            @Parameter(description = "위시리스트 항목 정보", required = true)
            @RequestBody WishRequest wishRequest,
            @Parameter(description = "로그인한 사용자 정보", required = true)
            @LoginMember User loginUser) {
        wishService.saveWish(wishRequest.getProductId(), loginUser.getId(), wishRequest.getAmount());
    }

    @Operation(summary = "위시리스트 항목 수정", description = "위시리스트 항목을 수정합니다.")
    @PutMapping("/{wishId}")
    @ResponseStatus(HttpStatus.OK)
    public void modifyWish(
            @Parameter(description = "수정할 위시리스트 항목의 ID", required = true)
            @PathVariable("wishId") Long wishId,
            @Parameter(description = "위시리스트 항목 정보", required = true)
            @RequestBody WishRequest wishRequest,
            @Parameter(description = "로그인한 사용자 정보", required = true)
            @LoginMember User loginUser) {
        wishService.modifyWish(wishId, wishRequest.getProductId(), loginUser.getId(), wishRequest.getAmount());
    }

    @Operation(summary = "위시리스트 조회", description = "로그인한 사용자의 위시리스트를 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<WishResponse>> getWishList(
            @Parameter(description = "로그인한 사용자 정보", required = true)
            @LoginMember User loginUser, Pageable pageable) {
        Page<Wish> wishes = wishService.getWishList(loginUser.getId(), pageable);
        Page<WishResponse> responses = wishes.map(WishResponse::fromModel);
        return ResponseEntity.ok().body(responses);
    }

    @Operation(summary = "위시리스트 항목 상세 조회", description = "위시리스트 항목의 상세 정보를 조회합니다.")
    @GetMapping("/{wishId}")
    public ResponseEntity<WishResponse> getWishDetail(
            @Parameter(description = "조회할 위시리스트 항목의 ID", required = true)
            @PathVariable("wishId") Long wishId,
            @Parameter(description = "로그인한 사용자 정보", required = true)
            @LoginMember User loginUser) {
        Wish wish = wishService.getWishDetail(wishId, loginUser.getId());
        return ResponseEntity.ok().body(WishResponse.fromModel(wish));
    }

    @Operation(summary = "위시리스트 항목 삭제", description = "위시리스트 항목을 삭제합니다.")
    @DeleteMapping("/{wishId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWish(
            @Parameter(description = "삭제할 위시리스트 항목의 ID", required = true)
            @PathVariable("wishId") Long wishId,
            @Parameter(description = "로그인한 사용자 정보", required = true)
            @LoginMember User loginUser) {
        wishService.deleteWish(wishId, loginUser.getId());
    }
}
