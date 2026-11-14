# LabelLens — Plain-Language Result Rebuild Report

- **Task ID**: `labellens-20261109-plain-result`
- **Requested By**: Muse
- **Date**: 2026-11-09
- **Status**: DONE

---

## 1. Executive Summary

This task completely rebuilt the LabelLens Result screen experience across two repositories (`LabelLensBackend` and `MobileApp`) to match the Kien-approved ChatGPT preview `result-plain-v1.png`.
Previously, the result screen displayed complete multi-line sentences as row titles and whole paragraphs as detail screen headers.
The result screen now presents:
1. **Header Bar**: Back arrow + "Phân tích nhãn" / "Label analysis" (+ delete / favorite actions).
2. **Product Card**: Photo thumbnail (when scan captured one, else category icon) + product name + subtitle chip.
3. **Verdict Card**: Light green container with leaf icon and exactly ONE plain sentence summary (no model ids, no tier text, no scores).
4. **Personal Warning Banner**: Light red container with warning icon and small label "Cảnh báo cho bạn" / "Warning for you", shown strictly for `CONTAINS_YOURS` and `CANNOT_RULE_OUT` fit states, with text derived directly from string resources.
5. **Section "Điểm tốt" / "Good to know"**: Tinted circular line icons + short title labels (never sentences) + value chip + everyday comparison line (e.g. "0 thìa cà phê", "≈ 2% nhu cầu/ngày" with slim progress bar, "≈ nửa hũ sữa chua") + tap chevron.
6. **Section "Cần để ý" / "Worth noting"**: Amber tinted icons, with red tinted styling for personal warnings.
7. **Section "Thành phần chính" / "Main ingredients"**: Clean list of ingredients with plain gloss explanations from the knowledge base when available.
8. **Removed**: The old "N ingredients analysed • ..." count line.
9. **Detail Screen**: Title is strictly the ingredient's short title label; definition and "who may want to avoid" behave cleanly as approved in Lane B.

---

## 2. Repositories & Git Commits

- **Backend Repository** (`LabelLensBackend`):
  - Branch: `feature/plain-result`
  - Commit: `59626ba` (`feat(backend): structured section items with plain labels and everyday comparisons`)
- **App Repository** (`LabelLens`):
  - Branch: `feature/plain-result`
  - Commit: `d78efb3` (`feat(app): plain-language result screen redesign with everyday comparisons`)

---

## 3. Backend Implementation & Live Deployment

### 3.1 Changes
- Structured section items: `title` (short label, max ~6 words), `value` (e.g. "36 mg"), `compare` (everyday comparison), `detail` (1-3 sentences nuance), `ingredient`.
- Summary constraint: `summary` constrained to 1 short plain sentence (max ~20 words) for the verdict card.
- Defensive parsing & sentence-title guard: Splits long titles at delimiters (`. `, `—`, `; `) to guarantee sentence-length strings never become row titles.
- Prompt builders updated for DeepSeek-first path and Mistral/Llama fallback path.

### 3.2 Backend Tests (vitest)
- Test files: 7 passed (7)
- Tests: 47 passed (47), 0 failures
- TypeScript typecheck: 0 errors

### 3.3 Worker Deployment & Live Smoke Test
- Deployed Worker version: `59f74283-dcd3-4bc0-974e-af4ae08eb6b5` to `https://labellens-backend.appdexter.workers.dev`
- Live Smoke Test Request (Vietnamese fresh milk with profile allergy `milk` and language `vi`):
```json
{
  "productType": "food",
  "ocrText": "SỮA TƯƠI TIỆT TRÙNG NGUYÊN CHẤT TH TRUE MILK\nThành phần: Sữa hoàn toàn từ sữa bò tươi (99,8%), đường, chất ổn định (471, 407, 412).\nNăng lượng: 70 kcal\nChất đạm: 3,0 g\nChất béo: 3,2 g\nCarbonhydrat: 7,2 g\nĐường: 7,2 g\nCanxi: 100 mg\nNatri: 40 mg\nThể tích thực: 180 ml",
  "language": "vi",
  "userProfile": {
    "allergies": ["peanut", "milk"]
  }
}
```
- Live Smoke Response:
```json
{
  "tierUsed": 2,
  "escalated": false,
  "tier3": "skipped_low_confidence_ok",
  "model": "deepseek-chat",
  "confidence": 0.95,
  "data": {
    "summary": "Sữa tươi tiệt trùng 100% nguyên chất, không chất bảo quản — nhưng có chứa sữa (dị ứng).",
    "fields": {
      "productName": "TH true MILK Sữa tươi tiệt trùng nguyên chất 100% 180 ml",
      "brand": "TH true MILK",
      "servingSize": "100 ml",
      "calories": "60,1 kcal",
      "totalFat": "3,3 g",
      "sodium": "42 mg",
      "totalCarbs": "4,6 g",
      "sugars": null,
      "protein": "3,0 g",
      "ingredients": [
        "sữa bò tươi nguyên chất (100%)"
      ],
      "allergens": [
        "sữa"
      ]
    },
    "sections": {
      "goodToKnow": [
        {
          "title": "Sữa bò tươi 100%",
          "value": "100%",
          "compare": "",
          "detail": "Sản phẩm được làm hoàn toàn từ sữa bò tươi nguyên chất, không pha loãng hay phối trộn nguyên liệu khác.",
          "ingredient": "sữa bò tươi nguyên chất (100%)"
        },
        {
          "title": "Không chất bảo quản",
          "value": "",
          "compare": "",
          "detail": "Nhãn ghi rõ không sử dụng chất bảo quản; độ an toàn được đảm bảo nhờ quy trình tiệt trùng.",
          "ingredient": ""
        },
        {
          "title": "Chất đạm",
          "value": "3,0 g/100 ml",
          "compare": "≈ 5% nhu cầu/ngày",
          "detail": "Lượng đạm khá tốt cho sữa tươi, hỗ trợ xây dựng cơ và duy trì năng lượng.",
          "ingredient": ""
        },
        {
          "title": "Canxi",
          "value": "104 mg/100 ml",
          "compare": "≈ 10% nhu cầu/ngày",
          "detail": "Canxi tự nhiên từ sữa bò, tốt cho xương và răng.",
          "ingredient": ""
        }
      ],
      "worthNoting": [
        {
          "title": "Năng lượng",
          "value": "60,1 kcal/100 ml",
          "compare": "≈ 1/4 bát cơm",
          "detail": "Mức năng lượng vừa phải, phù hợp cho bữa phụ hoặc bổ sung dinh dưỡng hằng ngày.",
          "ingredient": ""
        },
        {
          "title": "Chất béo",
          "value": "3,3 g/100 ml",
          "compare": "",
          "detail": "Chất béo tự nhiên có trong sữa nguyên kem, góp phần tạo vị béo và cung cấp năng lượng.",
          "ingredient": ""
        },
        {
          "title": "Hydrat cacbon",
          "value": "4,6 g/100 ml",
          "compare": "≈ 1 thìa cà phê đường",
          "detail": "Lượng carb chủ yếu là lactose tự nhiên của sữa, không phải đường thêm vào.",
          "ingredient": ""
        },
        {
          "title": "Khẩu phần 180 ml",
          "value": "180 ml",
          "compare": "",
          "detail": "Mỗi hộp 180 ml; giá trị dinh dưỡng trên nhãn tính theo 100 ml, nên nhân khoảng 1,8 lần khi dùng cả hộp.",
          "ingredient": ""
        }
      ],
      "watchOut": [
        {
          "title": "Chứa sữa (dị ứng)",
          "value": "",
          "compare": "",
          "detail": "Sản phẩm chứa sữa bò — người dị ứng hoặc không dung nạp lactose cần lưu ý và tránh sử dụng.",
          "ingredient": "sữa bò tươi nguyên chất (100%)"
        },
        {
          "title": "Natri",
          "value": "42 mg/100 ml",
          "compare": "≈ 2% nhu cầu/ngày",
          "detail": "Natri ở mức thấp, không đáng lo với người khỏe mạnh; người cần kiểm soát muối vẫn nên tính vào tổng khẩu phần.",
          "ingredient": ""
        }
      ]
    }
  }
}
```

---

## 4. App Implementation & Verification

### 4.1 Parser Tolerance & Local Comparisons
- `BackendSectionItemSerializer`: Decodes both new object items and legacy string items. Sentence-title guard ensures no sentence ever becomes a title.
- `LocalComparisonHelper`: Deterministic local everyday comparisons:
  - Sugar: grams / 4 rounded to nearest 0.5 teaspoons (`compare_sugar_teaspoons` or `compare_sugar_zero`).
  - Sodium: `mg / 2300` percentage of daily needs when %DV is not on label.
  - Slim progress bars rendered only for percentages (0-100%).
- `IngredientDetailScreen`: Title displays only the short label (`extractFirstClause`).

### 4.2 App Test Results (Exact counts from XML reports)
- `jvmTest`: 40 suites, 271 tests, 0 failures, 0 skipped in 3.73s
- `testAndroidHostTest`: 35 suites, 258 tests, 0 failures, 0 skipped in 6.09s
- Total App Tests: 75 suites, 529 tests, 0 failures, 0 skipped
- Formatting: `spotlessCheck` passed cleanly (0 violations)
- Compilation: `:androidApp:assembleDebug` completed successfully

### 4.3 Emulator QA Verification (`emulator-5560`)
All screenshots saved to `Docs/qa-v2/plain-result/`:
1. **Food Result with AI Items (Vietnamese)**:
   - Top: `Docs/qa-v2/plain-result/food_ai_vi_top.png` (Product card thumbnail, 1-sentence verdict card, personal warning banner for milk allergy).
   - Mid: `Docs/qa-v2/plain-result/food_ai_vi_mid.png` (Section "Điểm tốt" with short titles, value chips, percentage progress bar comparison `≈ 16% nhu cầu/ngày`).
   - Bottom: `Docs/qa-v2/plain-result/food_ai_vi_bottom.png` (Section "Cần để ý" with `≈ 8 thìa cà phê đường`, "Thành phần chính" plain glosses, no count line).
   - Detail tap: `Docs/qa-v2/plain-result/food_detail_sel.png` (Detail screen title is short label "sel", not a sentence).
   - Built-in food sample: `food_vi_top.png`, `food_vi_mid.png`, `food_vi_bottom.png`.
2. **Cosmetics Sample (Clony, local-only)**:
   - `Docs/qa-v2/plain-result/cosmetics_clony_result.png` (Plain layout with shampoo category, verdict, Good to know, Main ingredients).
3. **Supplement Sample (Vitamin D3)**:
   - `Docs/qa-v2/plain-result/supplement_vitd3_result.png` (Educational disclaimer card kept intact, unified plain row layout).
4. **Font Scale 1.3**:
   - Top: `Docs/qa-v2/plain-result/result_fontscale13_top.png`
   - Bottom: `Docs/qa-v2/plain-result/result_fontscale13_bottom.png`
   - System font scale restored to 1.0.
5. **English Result**:
   - `Docs/qa-v2/plain-result/result_en.png` ("Label Analysis", "Good to know", "Worth noting", everyday comparisons in English).
