package egovframework.example.document.controller;
import egovframework.example.auth.domain.SessionActor;
import egovframework.example.auth.service.SessionBoundaryInterceptor;
import egovframework.example.common.response.ApiResponse;
import egovframework.example.document.dto.DocumentModels.*;
import egovframework.example.document.service.ManagedDocumentService;
import egovframework.example.document.service.DocumentSearchService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.core.env.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/documents")
public class ManagedDocumentController {
 private final ManagedDocumentService service;private final DocumentSearchService search;private final Environment environment;
 public ManagedDocumentController(ManagedDocumentService service,DocumentSearchService search,Environment environment){this.service=service;this.search=search;this.environment=environment;}
 @ModelAttribute void label(HttpServletResponse response){if(environment.acceptsProfiles(Profiles.of("local-mock")))response.setHeader("X-Demo-Data","SYNTHETIC_EPHEMERAL");}
 private SessionActor actor(HttpServletRequest r){return (SessionActor)r.getAttribute(SessionBoundaryInterceptor.SESSION_ACTOR);}
 @PostMapping public ApiResponse<String> create(HttpServletRequest r,@Valid @RequestBody Create input){return ApiResponse.of(service.create(actor(r),input));}
 @PostMapping("/{code}/versions") public ApiResponse<Integer> version(HttpServletRequest r,@PathVariable String code,@Valid @RequestBody Version input){return ApiResponse.of(service.addVersion(actor(r),code,input));}
 @PutMapping("/{code}/versions/draft") public ApiResponse<Boolean> draft(HttpServletRequest r,@PathVariable String code,@Valid @RequestBody Version input){service.replaceDraft(actor(r),code,input);return ApiResponse.of(true);}
 @PostMapping("/{code}/review-request") public ApiResponse<Boolean> review(HttpServletRequest r,@PathVariable String code,@Valid @RequestBody Review input){service.requestReview(actor(r),code,input);return ApiResponse.of(true);}
 @PostMapping("/{code}/approve") public ApiResponse<Boolean> approve(HttpServletRequest r,@PathVariable String code){service.decide(actor(r),code,true);return ApiResponse.of(true);}
 @PostMapping("/{code}/reject") public ApiResponse<Boolean> reject(HttpServletRequest r,@PathVariable String code){service.decide(actor(r),code,false);return ApiResponse.of(true);}
 @PostMapping("/{code}/withdraw") public ApiResponse<Boolean> withdraw(HttpServletRequest r,@PathVariable String code){service.withdraw(actor(r),code);return ApiResponse.of(true);}
 @PostMapping("/{code}/grants") public ApiResponse<Boolean> grant(HttpServletRequest r,@PathVariable String code,@Valid @RequestBody Grant input){service.grant(actor(r),code,input,false);return ApiResponse.of(true);}
 @DeleteMapping("/{code}/grants") public ApiResponse<Boolean> revoke(HttpServletRequest r,@PathVariable String code,@Valid @RequestBody Grant input){service.grant(actor(r),code,input,true);return ApiResponse.of(true);}
 @GetMapping("/{code}/published") public ApiResponse<Published> published(HttpServletRequest r,@PathVariable String code){return ApiResponse.of(service.published(actor(r),code));}
 @GetMapping("/search") public ApiResponse<SearchPage> search(HttpServletRequest r,@RequestParam(name="q") String query,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return ApiResponse.of(search.search(actor(r),query,page,size));}
 @GetMapping("/{id}/versions/{versionId}/fragments/{fragmentId}") public ApiResponse<SourceFragment> source(HttpServletRequest r,@PathVariable long id,@PathVariable long versionId,@PathVariable long fragmentId){return ApiResponse.of(search.source(actor(r),id,versionId,fragmentId));}
}
