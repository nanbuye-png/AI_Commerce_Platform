import hmac

from fastapi import Header, HTTPException, status

from app.config import INTERNAL_API_TOKEN


def require_internal_token(
    x_internal_token: str | None = Header(default=None),
) -> None:
    if not INTERNAL_API_TOKEN:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Internal service authentication is not configured",
        )

    if x_internal_token is None or not hmac.compare_digest(
        x_internal_token.encode("utf-8"),
        INTERNAL_API_TOKEN.encode("utf-8"),
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid internal service credentials",
        )
