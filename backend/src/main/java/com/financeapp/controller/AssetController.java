package com.financeapp.controller;

import com.financeapp.dto.AssetDto;
import com.financeapp.entity.Asset;
import com.financeapp.entity.User;
import com.financeapp.repository.AssetRepository;
import com.financeapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/assets")
@CrossOrigin(origins = "http://localhost:5173")
public class AssetController {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    public AssetController(AssetRepository assetRepository, UserRepository userRepository) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
    }

    // GET all assets
    @GetMapping
    public List<AssetDto.Response> getAllAssets(@PathVariable Long userId,
            @RequestParam(required = false) String type) {
        findUserOrThrow(userId);
        if (type != null && !type.isBlank()) {
            return assetRepository.findByUserIdAndType(userId, type)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return assetRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // GET single asset
    @GetMapping("/{id}")
    public AssetDto.Response getAssetById(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        return toResponse(findAssetOrThrow(id, userId));
    }

    // POST create asset
    @PostMapping
    public ResponseEntity<AssetDto.Response> createAsset(@PathVariable Long userId,
            @RequestBody AssetDto.Request request) {
        User user = findUserOrThrow(userId);
        Asset asset = new Asset(
                user,
                request.getName(),
                request.getType(),
                request.getBalance());

        Asset saved = assetRepository.save(asset);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // PUT update asset
    @PutMapping("/{id}")
    public AssetDto.Response updateAsset(@PathVariable Long userId, @PathVariable Long id,
            @RequestBody AssetDto.Request request) {
        findUserOrThrow(userId);
        Asset asset = findAssetOrThrow(id, userId);
        asset.setName(request.getName());
        asset.setType(request.getType());
        asset.setBalance(request.getBalance());
        return toResponse(assetRepository.save(asset));
    }

    // DELETE asset
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long userId, @PathVariable Long id) {
        findUserOrThrow(userId);
        findAssetOrThrow(id, userId);
        assetRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private Asset findAssetOrThrow(Long id, Long userId) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found with id: " + id));
        if (!asset.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("Asset not found with id: " + id);
        }
        return asset;
    }

    private AssetDto.Response toResponse(Asset asset) {
        AssetDto.Response response = new AssetDto.Response();
        response.setId(asset.getId());
        response.setUserId(asset.getUser().getId());
        response.setName(asset.getName());
        response.setType(asset.getType());
        response.setBalance(asset.getBalance());
        response.setUpdatedAt(asset.getUpdatedAt());
        return response;
    }
}