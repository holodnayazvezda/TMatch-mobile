# MVP Backend API Contracts

## Scope (v1)
- Регистрация/вход.
- Заполнение и редактирование профиля (в т.ч. обязательный gender).
- Настройка таргетинга (правила поиска: регион, пол, возраст).
- Получение рекомендаций.
- Лайк/дизлайк/блок/разблок кандидата (action: like, dislike, block, unblock).
- Активные мэтчи (взаимный like, без активного block) и получение списка своих мэтчей.
- Входящие лайки: список пользователей, которые лайкнули меня и ждут моего ответа (без push/FSM в MVP).
- Превью для жюри: 5 случайных карточек пользователей без авторизации.
- Аудит действий (append-only).

## OpenAPI 3.1 (draft)

```yaml
openapi: 3.1.0
info:
  title: T-Match MVP API
  version: 0.1.0
servers:
  - url: https://api.example.com

tags:
  - name: auth
  - name: profile
  - name: targeting
  - name: dating-profile
  - name: recommendations
  - name: matches
  - name: likes
  - name: chats
  - name: preview
  - name: interactions
  - name: notifications
  - name: metrics
  - name: health

paths:
  /auth/registration/email:
    post:
      tags: [auth]
      summary: Register by email/password
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SignUpEmailRequest'
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '409':
          description: Email already exists
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /auth/registration/yandex:
    post:
      tags: [auth]
      summary: Register with Yandex
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/YandexSignUpRequest'
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '401':
          description: Authentication failed
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /auth/signing/email:
    post:
      tags: [auth]
      summary: Sign in (email/password)
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SignInEmailRequest'
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          description: Invalid credentials
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /auth/signing/yandex:
    post:
      tags: [auth]
      summary: Sign in (Yandex)
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/YandexSignInRequest'
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '401':
          description: Invalid code or authentication failed
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /profile:
    put:
      tags: [profile]
      summary: Create or update own profile
      description: |
        Create: all fields from body (first_name, birth_date, gender required; last_name, region, avatar_url optional).
        Update: full body is applied (first_name, last_name, birth_date, gender, region, avatar_url).
        On every upsert an audit event `profile.updated` is emitted.
      security:
        - BearerToken: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpsertProfileRequest'
      responses:
        '200':
          description: Profile upserted
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProfileResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '401':
          $ref: '#/components/responses/Unauthorized'

    get:
      tags: [profile]
      summary: Get own profile
      security:
        - BearerToken: []
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProfileResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          description: Profile not set yet
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /profile/card:
    get:
      tags: [profile]
      summary: Get own card (profile + dating profile)
      security:
        - BearerToken: []
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SelfCardResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'

  /targeting/me:
    get:
      tags: [targeting]
      summary: Get own targeting rules
      description: |
        If targeting is absent, backend auto-creates defaults:
        gender_target=both, age_from=max(0, user_age-2), age_to=user_age+2, region from profile (or null).
      security:
        - BearerToken: []
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TargetingResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'

  /chats/me:
    get:
      tags: [chats]
      summary: Get own chats
      description: |
        Returns list of chats where current user is a participant. No unread counters or read-status in MVP.
      security:
        - BearerToken: []
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ChatsResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'

  /chats/{chat_id}/messages:
    get:
      tags: [chats]
      summary: Get chat messages
      description: |
        Returns paginated list of messages for given chat, ordered from oldest to newest.
      security:
        - BearerToken: []
      parameters:
        - name: chat_id
          in: path
          required: true
          schema:
            type: string
            format: uuid
        - name: limit
          in: query
          required: false
          schema:
            type: integer
            minimum: 1
            maximum: 200
        - name: before_message_id
          in: query
          required: false
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ChatMessagesResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          description: Chat access denied (user is not participant)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '404':
          description: Chat not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
    post:
      tags: [chats]
      summary: Send text message to chat
      description: |
        Sends text message to existing 1:1 chat. Requires active match between participants and no active block in either direction.
      security:
        - BearerToken: []
      parameters:
        - name: chat_id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SendMessageRequest'
      responses:
        '200':
          description: Message sent
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ChatMessageItemResponse'
        '400':
          description: Invalid message or no active match between participants
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          description: Chat access denied or chat is blocked
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '404':
          description: Chat not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
    put:
      tags: [targeting]
      summary: Create or update own targeting rules
      description: |
        Rules: region (optional), gender_target (male|female|both), age_from, age_to.
        age_from and age_to are required. Validation: age_from >= 0, age_to >= age_from.
      security:
        - BearerToken: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpsertTargetingRequest'
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TargetingResponse'
        '400':
          description: Invalid targeting rules (e.g. age_from >= 0, age_to >= age_from)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '401':
          $ref: '#/components/responses/Unauthorized'

  /profile/card/{user_id}:
    get:
      tags: [profile]
      summary: Get user card by user id (profile + dating profile)
      security:
        - BearerToken: []
      parameters:
        - name: user_id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SelfCardResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          description: User not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /dating-profile/me:
    get:
      tags: [dating-profile]
      summary: Get own dating profile
      security:
        - BearerToken: []
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DatingProfileResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          description: Dating profile not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
    put:
      tags: [dating-profile]
      summary: Create or update own dating profile
      description: |
        Requires profile. At least one photo required.
        Replaces entire photo list.
      security:
        - BearerToken: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpsertDatingProfileRequest'
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DatingProfileResponse'
        '400':
          description: Profile required to create dating profile, or at least one photo required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '401':
          $ref: '#/components/responses/Unauthorized'

  /dating-profile/me/photos:
    post:
      tags: [dating-profile]
      summary: Upload dating profile photo
      description: |
        Requires bank profile. Request body is raw image bytes.
        Content-Type header required (image/jpeg, image/png, or image/webp). Max 10 MB.
        Returns URL to add to dating profile photos; removal is via PUT /dating-profile/me (replace list).
      security:
        - BearerToken: []
      requestBody:
        required: true
        content:
          image/jpeg:
            schema:
              type: string
              format: binary
          image/png:
            schema:
              type: string
              format: binary
          image/webp:
            schema:
              type: string
              format: binary
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UploadDatingPhotoResponse'
        '400':
          description: Profile required to upload dating photo
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '413':
          description: Photo too large (max 10 MB)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '415':
          description: Invalid or missing Content-Type / invalid payload
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '503':
          description: Photo storage unavailable
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /dating-profile/me/traits/visibility:
    patch:
      tags: [dating-profile]
      summary: Set trait visibility (hide/show)
      description: |
        User can only hide or show traits; cannot add or remove.
      security:
        - BearerToken: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SetTraitVisibilityRequest'
      responses:
        '204':
          description: No content
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          description: Dating profile or trait not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /recommendations:
    get:
      tags: [recommendations]
      summary: Get personalized candidates for current user
      description: |
        Requires a ready dating profile (exists and has at least one photo).
      security:
        - BearerToken: []
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RecommendationsResponse'
        '400':
          description: Profile not set yet or dating profile not ready (min 1 photo required)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '422':
          $ref: '#/components/responses/ValidationError'
        '503':
          description: ML provider unavailable or recommendation candidates not found in backend
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /matches/me:
    get:
      tags: [matches]
      summary: Get own active matches
      description: |
        Returns users with whom the current user has an active match.
        Active match: latest interaction in both directions (A→B and B→A) is `like`,
        and there is no active block between the pair (latest block/unblock per direction is not `block`).
        If either side's latest action becomes `dislike`, or either side has blocked the other, the match is no longer active.
      security:
        - BearerToken: []
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/MatchesResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'

  /likes/incoming/me:
    get:
      tags: [likes]
      summary: Get users who liked me and await my reply
      description: |
        Returns users who liked the current user and to whom the current user has not yet replied (no like/dislike from me to them).
        Rule: latest interaction B→me is `like`, and in direction me→B there is no reply (latest me→B is absent or not `like`/`dislike`).
        Users with an active block in either direction are excluded.
        MVP: API only; no push/FSM.
      security:
        - BearerToken: []
      responses:
        '200':
          description: OK (empty list if no incoming likes)
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/IncomingLikesResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'

  /preview/cards:
    get:
      tags: [preview]
      summary: Get 5 random user cards (preview for jury)
      description: |
        No auth. Returns 5 random user cards (profile + dating profile) for demo/jury preview.
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PreviewCardsResponse'

  /interactions:
    post:
      tags: [interactions]
      summary: Append interaction event for candidate
      description: |
        Interaction history is append-only.
        Actions: `like`, `dislike`, `block`, `unblock`.
        For `like` and `dislike` recommendation is optional: if provided, it is stored in audit payload and may be used by ML; interaction itself is allowed regardless of recommendation.
        For `block` and `unblock` no recommendation guard; effective block state per direction is defined by latest `block`/`unblock` event.
        Block excludes the pair from recommendations (both directions) and from active matches.
        Multiple interactions for same actor->candidate are allowed.
        Effective state for recommendations and matches is defined by latest interaction (and, for block, latest block/unblock per direction).
      security:
        - BearerToken: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateInteractionRequest'
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/InteractionResponse'
        '400':
          description: Cannot interact with yourself
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          description: Candidate not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
        '422':
          $ref: '#/components/responses/ValidationError'

  /notifications:
    post:
      tags: [notifications]
      summary: Send push notification
      security:
        - BearerToken: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SendNotificationRequest'
      responses:
        '200':
          description: OK
        '401':
          $ref: '#/components/responses/Unauthorized'
        '404':
          description: Notification device not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'

  /notifications/devices:
    post:
      tags: [notifications]
      summary: Register FCM device token
      security:
        - BearerToken: []
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterDeviceRequest'
      responses:
        '200':
          description: OK
        '401':
          $ref: '#/components/responses/Unauthorized'

  /metrics:
    get:
      tags: [metrics]
      summary: Prometheus metrics
      responses:
        '200':
          description: OK
          content:
            text/plain:
              schema:
                type: string

  /health:
    get:
      tags: [health]
      summary: Liveness probe
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/HealthResponse'

  /ready:
    get:
      tags: [health]
      summary: Readiness probe
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/HealthResponse'
        '503':
          description: Unavailable
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ReadyResponse'

components:
  securitySchemes:
    BearerToken:
      type: http
      scheme: bearer
      bearerFormat: JWT

  responses:
    BadRequest:
      description: Bad request
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/Error'
    Unauthorized:
      description: Unauthorized
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/Error'
    ValidationError:
      description: Validation error
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/Error'

  schemas:
    SignUpEmailRequest:
      type: object
      additionalProperties: false
      required: [email, password]
      properties:
        email:
          type: string
          format: email
          maxLength: 320
        password:
          type: string
          minLength: 8
          maxLength: 128

    SignInEmailRequest:
      type: object
      additionalProperties: false
      required: [email, password]
      properties:
        email:
          type: string
          format: email
          maxLength: 320
        password:
          type: string
          minLength: 1
          maxLength: 128

    YandexSignUpRequest:
      type: object
      additionalProperties: false
      required: [code]
      properties:
        code:
          type: string
          minLength: 1

    YandexSignInRequest:
      type: object
      additionalProperties: false
      required: [code]
      properties:
        code:
          type: string
          minLength: 1

    AuthResponse:
      type: object
      additionalProperties: false
      required: [user_id, access_token]
      properties:
        user_id:
          type: string
          format: uuid
        access_token:
          type: string

    Gender:
      type: string
      enum: [male, female]

    UpsertProfileRequest:
      type: object
      additionalProperties: false
      required: [first_name, birth_date, gender]
      properties:
        first_name:
          type: string
          minLength: 1
          maxLength: 80
        last_name:
          type: string
          minLength: 1
          maxLength: 80
          nullable: true
        birth_date:
          type: string
          format: date
        gender:
          $ref: '#/components/schemas/Gender'
        region:
          type: string
          minLength: 1
          maxLength: 120
          nullable: true
        avatar_url:
          type: string
          format: uri
          maxLength: 2048
          nullable: true

    ProfileResponse:
      type: object
      additionalProperties: false
      required: [user_id, first_name, birth_date, gender]
      properties:
        user_id:
          type: string
          format: uuid
        first_name:
          type: string
        last_name:
          type: string
          nullable: true
        birth_date:
          type: string
          format: date
        gender:
          $ref: '#/components/schemas/Gender'
        region:
          type: string
          nullable: true
        avatar_url:
          type: string
          format: uri
          nullable: true

    Profile:
      type: object
      additionalProperties: false
      required: [first_name, birth_date, gender]
      properties:
        first_name:
          type: string
        last_name:
          type: string
          nullable: true
        birth_date:
          type: string
          format: date
        gender:
          $ref: '#/components/schemas/Gender'
        region:
          type: string
          nullable: true
        avatar_url:
          type: string
          format: uri
          nullable: true

    TargetGender:
      type: string
      enum: [male, female, both]

    UpsertTargetingRequest:
      type: object
      additionalProperties: false
      required: [gender_target, age_from, age_to]
      properties:
        region:
          type: string
          minLength: 1
          maxLength: 120
          nullable: true
        gender_target:
          $ref: '#/components/schemas/TargetGender'
        age_from:
          type: integer
          minimum: 0
        age_to:
          type: integer
          minimum: 0

    TargetingResponse:
      type: object
      additionalProperties: false
      required: [region, gender_target, age_from, age_to]
      properties:
        region:
          type: string
          nullable: true
        gender_target:
          $ref: '#/components/schemas/TargetGender'
        age_from:
          type: integer
        age_to:
          type: integer

    ChatsResponse:
      type: object
      additionalProperties: false
      required: [items]
      properties:
        items:
          type: array
          items:
            $ref: '#/components/schemas/ChatItemResponse'

    ChatItemResponse:
      type: object
      additionalProperties: false
      required: [chat_id, other_user_id]
      properties:
        chat_id:
          type: string
          format: uuid
        other_user_id:
          type: string
          format: uuid
        last_message_id:
          type: string
          format: uuid
          nullable: true
        last_message_text:
          type: string
          nullable: true
        last_message_created_at:
          type: string
          format: date-time
          nullable: true
        other_user_card:
          allOf:
            - $ref: '#/components/schemas/SelfCardResponse'
          nullable: true

    ChatMessagesResponse:
      type: object
      additionalProperties: false
      required: [items]
      properties:
        items:
          type: array
          items:
            $ref: '#/components/schemas/ChatMessageItemResponse'

    ChatMessageItemResponse:
      type: object
      additionalProperties: false
      required: [message_id, chat_id, sender_user_id, text, created_at]
      properties:
        message_id:
          type: string
          format: uuid
        chat_id:
          type: string
          format: uuid
        sender_user_id:
          type: string
          format: uuid
        text:
          type: string
        created_at:
          type: string
          format: date-time

    SendMessageRequest:
      type: object
      additionalProperties: false
      required: [text]
      properties:
        text:
          type: string
          minLength: 1
          maxLength: 4000

    SelfCardResponse:
      type: object
      additionalProperties: false
      required: [user_id, profile, dating_profile]
      properties:
        user_id:
          type: string
          format: uuid
        profile:
          allOf:
            - $ref: '#/components/schemas/ProfileResponse'
          nullable: true
        dating_profile:
          allOf:
            - $ref: '#/components/schemas/DatingProfileResponse'
          nullable: true

    RecommendationsResponse:
      type: object
      additionalProperties: false
      required: [items]
      properties:
        items:
          type: array
          items:
            $ref: '#/components/schemas/Recommendation'

    MatchesResponse:
      type: object
      additionalProperties: false
      required: [items]
      properties:
        items:
          type: array
          items:
            $ref: '#/components/schemas/MatchItem'

    MatchItem:
      type: object
      additionalProperties: false
      required: [candidate_user_id]
      properties:
        candidate_user_id:
          type: string
          format: uuid
        candidate_card:
          allOf:
            - $ref: '#/components/schemas/RecommendationCandidateCard'
          nullable: true

    IncomingLikesResponse:
      type: object
      additionalProperties: false
      required: [items]
      properties:
        items:
          type: array
          items:
            $ref: '#/components/schemas/IncomingLikeItem'

    IncomingLikeItem:
      type: object
      additionalProperties: false
      required: [liker_user_id]
      properties:
        liker_user_id:
          type: string
          format: uuid
        candidate_card:
          allOf:
            - $ref: '#/components/schemas/RecommendationCandidateCard'
          nullable: true
        reasons:
          type: object
          additionalProperties:
            type: number
            format: float
          description: |
            Optional ML reasons for this like: feature name → contribution to score.

    PreviewCardsResponse:
      type: object
      additionalProperties: false
      required: [cards]
      properties:
        cards:
          type: array
          items:
            $ref: '#/components/schemas/RecommendationCandidateCard'

    Recommendation:
      type: object
      additionalProperties: false
      required: [ml_recommendation_id, user_id, candidate_user_id, reasons]
      properties:
        ml_recommendation_id:
          type: string
        user_id:
          type: string
          format: uuid
        candidate_user_id:
          type: string
          format: uuid
        reasons:
          type: object
          minProperties: 1
          description: Произвольные строковые ключи (названия фич от ML), значения — вклад в скор (float). Валидация названий фич не выполняется.
          additionalProperties:
            type: number
            format: float
        candidate_card:
          allOf:
            - $ref: '#/components/schemas/RecommendationCandidateCard'
          nullable: true

    RecommendationCandidateCard:
      type: object
      additionalProperties: false
      required: [user_id, profile, dating_profile]
      properties:
        user_id:
          type: string
          format: uuid
        profile:
          allOf:
            - $ref: '#/components/schemas/Profile'
          nullable: true
        dating_profile:
          allOf:
            - $ref: '#/components/schemas/RecommendationCandidateDatingProfile'
          nullable: true

    RecommendationCandidateDatingProfile:
      type: object
      additionalProperties: false
      required: [photos, traits]
      properties:
        photos:
          type: array
          items:
            type: string
        traits:
          type: array
          items:
            $ref: '#/components/schemas/RecommendationCandidateDatingTrait'

    RecommendationCandidateDatingTrait:
      type: object
      additionalProperties: false
      required: [trait_code, score, is_hidden]
      properties:
        trait_code:
          type: string
        score:
          type: number
          format: float
        is_hidden:
          type: boolean

    UpsertDatingProfileRequest:
      type: object
      additionalProperties: false
      required: [photos]
      properties:
        photos:
          type: array
          minItems: 1
          items:
            type: string
            maxLength: 2048

    DatingProfileResponse:
      type: object
      additionalProperties: false
      required: [user_id, photos, traits]
      properties:
        user_id:
          type: string
          format: uuid
        photos:
          type: array
          items:
            type: string
        traits:
          type: array
          items:
            type: object
            required: [trait_code, score, is_hidden]
            properties:
              trait_code:
                type: string
              score:
                type: number
                format: float
              is_hidden:
                type: boolean

    SetTraitVisibilityRequest:
      type: object
      additionalProperties: false
      required: [trait_code, is_hidden]
      properties:
        trait_code:
          type: string
          minLength: 1
          maxLength: 64
        is_hidden:
          type: boolean

    UploadDatingPhotoResponse:
      type: object
      additionalProperties: false
      required: [photo_url]
      properties:
        photo_url:
          type: string
          format: uri

    CreateInteractionRequest:
      type: object
      additionalProperties: false
      required: [candidate_user_id, action]
      properties:
        candidate_user_id:
          type: string
          format: uuid
        action:
          type: string
          enum: [like, dislike, block, unblock]
        ml_recommendation_id:
          type: string
          nullable: true
          description: Optional link to recommendation (required for like/dislike from feed); block/unblock may omit.

    InteractionResponse:
      type: object
      additionalProperties: false
      required: [interaction_id, actor_user_id, candidate_user_id, action, created_at]
      properties:
        interaction_id:
          type: string
          format: uuid
        actor_user_id:
          type: string
          format: uuid
        candidate_user_id:
          type: string
          format: uuid
        action:
          type: string
          enum: [like, dislike, block, unblock]
        created_at:
          type: string
          format: date-time
        ml_recommendation_id:
          type: string
          nullable: true

    SendNotificationRequest:
      type: object
      additionalProperties: false
      required: [title, body]
      properties:
        title:
          type: string
          minLength: 1
          maxLength: 100
        body:
          type: string
          minLength: 1
          maxLength: 500
        data:
          type: object
          additionalProperties:
            type: string
          description: |
            Optional custom key-value payload passed to FCM `data` field.
            Used by the mobile client to determine notification type and navigation target.

            Known payloads (backend -> mobile):
            - For chat message push (type=MESSAGE):
              - chat_id: string (uuid) — chat identifier
              - sender_user_id: string (uuid) — sender user id
              - sender_name: string — sender display name (may be "New message" fallback)
              - text: string — message text

    RegisterDeviceRequest:
      type: object
      additionalProperties: false
      required: [device_id]
      properties:
        device_id:
          type: string
          minLength: 1
          maxLength: 200

    HealthResponse:
      type: object
      additionalProperties: false
      required: [status]
      properties:
        status:
          type: string
          enum: [ok]

    ReadyResponse:
      type: object
      additionalProperties: false
      required: [status]
      properties:
        status:
          type: string
          enum: [ok, unavailable]

    Error:
      type: object
      additionalProperties: true
      description: |
        HTTP error body. Litestar returns key `detail` (string) with the error message.
        For validation errors (422) `detail` may be an array of constraint violations.
      properties:
        detail:
          oneOf:
            - type: string
            - type: array
              items:
                type: object
```

## WebSocket Chat Stream

WebSocket-канал для доставки входящих сообщений чата в реальном времени.

### Endpoint

- `ws://{host}/chat/stream?token={access_token}`
  - `access_token` — raw access token, полученный из `AuthResponse.access_token`.
  - Если токен не передан или недействителен/просрочен, сервер закрывает соединение с кодом `WS_1008_POLICY_VIOLATION`.

### Сообщения Server → Client

Единственный тип — новое сообщение в чате. Формат JSON:

```json
{
  "type": "message",
  "message_id": "uuid",
  "chat_id": "uuid",
  "sender_user_id": "uuid",
  "text": "string",
  "created_at": "2026-03-16T12:00:00.000000Z"
}
```

| Поле           | Тип               | Описание                                    |
| -------------- | ----------------- | ------------------------------------------- |
| type           | string            | Всегда `"message"`                          |
| message_id     | string (uuid)     | Идентификатор сообщения                     |
| chat_id        | string (uuid)     | Идентификатор чата                          |
| sender_user_id | string (uuid)     | Идентификатор отправителя                   |
| text           | string            | Текст сообщения                             |
| created_at     | string (ISO 8601) | Время создания сообщения (UTC, ISO 8601)    |

### Сообщения Client → Server

На текущем MVP клиент только получает события; отправка сообщений в чат выполняется через HTTP `POST /chats/{chat_id}/messages`. Если позже появятся команды (ping, типы подписок и т.п.), они будут описаны в этом разделе.

---

## Internal API (backend -> ML)

Контракт для интеграции backend с ML-сервисом. Backend выступает клиентом: запрашивает рекомендации у ML.

### POST /ml/recommendations

Backend запрашивает список кандидатов для пользователя. Формат ответа совпадает с DTO в backend (`RecommendationItem`).

**Request** (backend -> ML):

| Field    | Type   | Required | Description      |
|----------|--------|----------|------------------|
| user_id  | string | yes      | UUID пользователя |

**Response** (ML -> backend), 200 OK:

| Field   | Type  | Description |
|---------|-------|-------------|
| response[]                    | array  | Список рекомендаций |
| response[].ml_recommendation_id | string | Идентификатор рекомендации от ML |
| response[].user_id              | string | UUID пользователя, для которого сформирована рекомендация |
| response[].candidate_user_id    | string | UUID кандидата (format: uuid) |
| response[].reasons              | object | Причины/ранжирование: произвольные строковые ключи (фичи от ML) → number (вклад в скор) |
| response[].reasons.*            | number | Вклад фичи в скор (float) |

**Response schemas (JSON):**

```yaml
# Request body (backend sends to ML)
MlRecommendationsRequest:
  type: object
  required: [user_id]
  properties:
    user_id:
      type: string
      format: uuid

# Response body (ML returns to backend)
MlRecommendationsResponse:
  type: array
  items:
    type: object
    required: [ml_recommendation_id, user_id, candidate_user_id, reasons]
    properties:
      ml_recommendation_id:
        type: string
      user_id:
        type: string
        format: uuid
      candidate_user_id:
        type: string
        format: uuid
      reasons:
        type: object
        minProperties: 1
        description: Произвольные строковые ключи (фичи от ML), значения — float. При невалидной структуре backend возвращает 503 и логирует причину.
        additionalProperties:
          type: number
          format: float
```

**Status codes:** `200` OK, `400` Bad request, `401` Unauthorized (если ML требует ключ), `503` Service unavailable.

## Domain Invariants (MVP)
- **Active match**: мэтч активен тогда и только тогда, когда последнее взаимодействие в обе стороны (A→B и B→A) имеет тип `like` и между парой нет активного блока (последнее событие block/unblock по каждому направлению не должно быть `block`). Если последнее действие любой из сторон — `dislike`, или есть активный block в любую сторону, мэтч не считается активным. Список активных мэтчей вычисляется on-read по таблице interactions (отдельной таблицы matches на MVP нет).
- Пользователь без заполненного `profile` не может получать рекомендации.
- `profile` хранится в домене как VO внутри `User`; обновление профиля не создает отдельную доменную сущность.
- В профиле поле `gender` обязательно (не null); значения — enum (male, female).
- После создания профиля при обновлении передаются все поля (first_name, last_name, birth_date, gender, region, avatar_url).
- Targeting — отдельная сущность (1:1 с user); правила: region (опционально), gender_target (male|female|both), age_from, age_to. Для пользователя без targeting backend автосоздает дефолт: gender_target=both, age_from=max(0, user_age-2), age_to=user_age+2, region из профиля (или null). Валидация upsert: age_from >= 0, age_to >= age_from.
- При создании/обновлении профиля создается audit event `profile.updated`.
- Каждая рекомендация содержит `ml_recommendation_id`, пришедший от ML; backend сохраняет этот id в БД вместе с Recommendation.
- Для получения рекомендаций нужна готовая dating-анкета (существует и содержит минимум 1 фото).
- Создать dating-анкету можно только при наличии профиля; при создании/обновлении обязательно минимум 1 фото.
- Трейты в анкете добавляет/пересчитывает только ML; пользователь может только скрывать/показывать трейты (hide/show).
- `interaction` пишется append-only, переписывание прошлых записей запрещено.
- Для фильтрации выдачи по паре `(actor_user_id, candidate_user_id)` используется последнее interaction-событие.
- Действия interaction: `like`, `dislike`, `block`, `unblock`. Для `like` и `dislike` кандидат должен быть ранее рекомендован текущему пользователю; для `block` и `unblock` проверка рекомендации не требуется.
- Статус блокировки пары (A→B) определяется последним событием `block` или `unblock` в этом направлении: если последнее — `block`, пара считается заблокированной в направлении A→B. Заблокированная пара (в любую сторону) исключается из рекомендаций для обоих пользователей, не считается активным мэтчем и не попадает в список входящих лайков. После `unblock` ограничение снимается.
- **Incoming likes (кто лайкнул и ждёт ответа)**: пользователь B входит в список входящих лайков для пользователя A тогда и только тогда, когда (1) последнее взаимодействие B→A есть и имеет тип `like`, (2) в направлении A→B нет ответа: последнего взаимодействия нет или его тип не `like` и не `dislike`. Если между парой есть активный block в любую сторону, B не показывается в списке входящих лайков для A. Список вычисляется on-read по таблице interactions.
- Если последнее действие `dislike`, кандидат исключается из выдачи; если последнее `like`, кандидат может появляться снова по правилам ранжирования.
- Пользователь не может отправить interaction на самого себя.
- Пользователь может отправить interaction только по кандидату, который был рекомендован ему backend (по паре user_id + candidate_user_id и, при наличии, ml_recommendation_id) — только для action `like` и `dislike`; для `block` и `unblock` привязка к рекомендации не требуется.
- Привязка интеракции к рекомендации опциональна для block/unblock; для like/dislike из ленты передают `ml_recommendation_id`.
- `AuditEvent` пишется append-only.
- Отправка в ML идет пачками через `AuditEvent` и флаг `sent_to_ml`.

## Event Names (audit)
- `profile.updated`
- `interaction.created`
