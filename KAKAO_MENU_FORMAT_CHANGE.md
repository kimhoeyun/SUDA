# 카카오톡 챗봇 학식 메뉴 응답 포맷 개선 정리

## 1. 작업 배경

카카오톡 챗봇에서 학생식단(아마랜스홀) 메뉴를 내려줄 때, `<선택식>`과 `<공통식>` 태그가 메뉴 본문과 같은 줄에 붙어 보여서 가독성이 떨어지는 문제가 있었다.

기존 출력 예시:

```text
✅ 학생식단(아마랜스홀)
<선택식> 수제등심돈까스
얼큰순대국
아비코카레덮밥 <공통식> 온두부
양배추샐러드&드레싱
볶음김치
```

원하는 출력 예시:

```text
✅ 학생식단(아마랜스홀)
<선택식>
수제등심돈까스
얼큰순대국
아비코카레덮밥

<공통식>
온두부
양배추샐러드&드레싱
볶음김치
```

---

## 2. 수정 목표

- `<선택식>` 뒤에는 항상 줄바꿈 1개가 들어가도록 처리
- `<공통식>` 앞에는 빈 줄 1개, 뒤에는 줄바꿈 1개가 들어가도록 처리
- 기존 메뉴 줄바꿈 구조는 최대한 유지
- 공백, CRLF/LF 혼용, 중복 줄바꿈이 있어도 안정적으로 정리
- 다른 식당 포맷에 부작용이 없도록 메뉴 정규화 helper 중심으로 최소 수정

---

## 3. 실제 수정한 파일

### 3-1. `src/main/java/com/suda/global/autocrawl/MealCrawler.java`

핵심 정규화 로직을 담당하는 파일이다.

- `cleanMenuText(...)`
  - 기존처럼 HTML을 텍스트로 정리한 뒤 `normalizeSpecialMenuText(...)`를 호출
  - 별도 큰 구조 변경 없이 helper 확장만 반영
- `normalizeSpecialMenuText(...)`
  - 기존에는 `(공통찬)` 앞 줄바꿈만 처리했음
  - 이번에 `<선택식>`, `<공통식>`, 공백/줄바꿈 정리까지 담당하도록 확장
  - 다른 곳에서도 재사용할 수 있도록 `public static`으로 변경

### 3-2. `src/main/java/com/suda/domain/meal/service/MealService.java`

카카오 응답 문자열을 최종 조합하는 파일이다.

- `buildResponseText(...)`
  - 기존에는 `meal.getMenu()`를 그대로 붙였음
  - 이제 `MealCrawler.normalizeSpecialMenuText(meal.getMenu())`를 한 번 더 적용한 뒤 응답 문자열에 붙임
  - 이미 저장된 메뉴 데이터나 예외적인 입력이 있어도 응답 단계에서 한 번 더 보정 가능

### 3-3. 테스트 파일

- `src/test/java/com/suda/global/autocrawl/MealCrawlerTest.java`
  - `<선택식>`, `<공통식>` 줄바꿈 정규화 테스트 추가
  - 공백/중복 줄바꿈 섞인 입력에 대한 안정성 테스트 추가
- `src/test/java/com/suda/domain/meal/service/MealServiceTest.java`
  - 카카오 응답 문자열 최종 결과 테스트 추가
  - 기존 기본 문구 테스트의 오래된 기대값도 현재 코드 기준으로 정리

---

## 4. 변경 전 / 변경 후

### 4-1. `MealCrawler.normalizeSpecialMenuText(...)`

변경 전:

```java
static String normalizeSpecialMenuText(String text) {
    return text.replaceAll(COMMON_SIDE_DISH_PATTERN, "\n(공통찬)");
}
```

변경 후:

```java
public static String normalizeSpecialMenuText(String text) {
    if (text == null || text.isBlank()) {
        return "";
    }

    String normalized = text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replaceAll(SELECTED_MENU_PATTERN, "<선택식>\n")
            .replaceAll(COMMON_MENU_PATTERN, "\n\n<공통식>\n")
            .replaceAll(COMMON_SIDE_DISH_PATTERN, "\n(공통찬)")
            .replaceAll("(?m)[\\t\\x0B\\f ]+$", "")
            .replaceAll("\\n{3,}", "\n\n");

    return normalized.trim();
}
```

### 4-2. `MealService.buildResponseText(...)`

변경 전:

```java
meals.forEach(meal -> {
    sb.append("✅ ")
            .append(meal.getCafeteriaName())
            .append("\n")
            .append(meal.getMenu())
            .append("\n\n");
});
```

변경 후:

```java
meals.forEach(meal -> {
    String normalizedMenu = MealCrawler.normalizeSpecialMenuText(meal.getMenu());

    sb.append("✅ ")
            .append(meal.getCafeteriaName())
            .append("\n")
            .append(normalizedMenu)
            .append("\n\n");
});
```

---

## 5. 최종 반영 코드

### 5-1. `MealCrawler`의 메뉴 정규화 코드

```java
private static final String COMMON_SIDE_DISH_PATTERN = "(?<=\\S)\\s+\\(공통찬\\)";
private static final String SELECTED_MENU_PATTERN = "\\h*<선택식>(?:\\h*\\R\\h*|\\h+)?";
private static final String COMMON_MENU_PATTERN = "(?:\\h*\\R\\h*)*\\h*<공통식>(?:\\h*\\R\\h*|\\h+)?";

private String cleanMenuText(Element td) {
    String html = td.html()
            .replaceAll(REGEX_BR, BR_TOKEN)
            .replace(HTML_SPACE, " ");

    String decoded = Parser.unescapeEntities(html, false);
    String normalizedText = Jsoup.parse(decoded).text().replace(BR_TOKEN, "\n");

    String result = Arrays.stream(normalizedText.split("\\R"))
            .map(String::trim)
            .filter(line -> !line.isBlank() && !line.equals("-"))
            .collect(Collectors.joining("\n"));

    return normalizeSpecialMenuText(result).trim();
}

public static String normalizeSpecialMenuText(String text) {
    if (text == null || text.isBlank()) {
        return "";
    }

    String normalized = text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replaceAll(SELECTED_MENU_PATTERN, "<선택식>\n")
            .replaceAll(COMMON_MENU_PATTERN, "\n\n<공통식>\n")
            .replaceAll(COMMON_SIDE_DISH_PATTERN, "\n(공통찬)")
            .replaceAll("(?m)[\\t\\x0B\\f ]+$", "")
            .replaceAll("\\n{3,}", "\n\n");

    return normalized.trim();
}
```

### 5-2. `MealService`의 카카오 응답 조합 코드

```java
public String buildResponseText(List<? extends MealInfo> meals) {
    if (meals == null || meals.isEmpty()) {
        return NO_MENU_MESSAGE;
    }

    String today = meals.get(0).getDayOfWeek();

    StringBuilder sb = new StringBuilder();
    sb.append(today).append(MENU_MESSAGE).append("\n\n");

    meals.forEach(meal -> {
        String normalizedMenu = MealCrawler.normalizeSpecialMenuText(meal.getMenu());

        sb.append("✅ ")
                .append(meal.getCafeteriaName())
                .append("\n")
                .append(normalizedMenu)
                .append("\n\n");
    });

    return sb.toString().trim();
}
```

---

## 6. 입력/출력 예시

입력:

```text
<선택식> 수제등심돈까스
얼큰순대국
아비코카레덮밥 <공통식> 온두부
양배추샐러드&드레싱
볶음김치
```

`normalizeSpecialMenuText(...)` 적용 후:

```text
<선택식>
수제등심돈까스
얼큰순대국
아비코카레덮밥

<공통식>
온두부
양배추샐러드&드레싱
볶음김치
```

최종 카카오 응답 예시:

```text
월요일학식 메뉴입니다 🍱

✅ 학생식단(아마랜스홀)
<선택식>
수제등심돈까스
얼큰순대국
아비코카레덮밥

<공통식>
온두부
양배추샐러드&드레싱
볶음김치
```

---

## 7. 이번 수정의 핵심 포인트

- 메뉴 문자열 정리를 한 군데 helper로 모아서 관리
- 크롤링 시점과 카카오 응답 시점 모두 같은 규칙 적용
- 단순 문자열 치환이 아니라 정규식 기반으로 공백/줄바꿈 혼용까지 보정
- 다른 메뉴 포맷에 영향이 적도록 `<선택식>`, `<공통식>`, `(공통찬)`만 타겟팅

---

## 8. 테스트 결과

실행한 테스트:

```powershell
.\gradlew.bat test --tests com.suda.global.autocrawl.MealCrawlerTest --tests com.suda.domain.meal.service.MealServiceTest
```

결과:

```text
BUILD SUCCESSFUL
```
