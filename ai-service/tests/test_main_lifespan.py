import asyncio


class ClosableProvider:
    def __init__(self) -> None:
        self.closed = False

    async def aclose(self) -> None:
        self.closed = True


def test_lifespan_closes_provider() -> None:
    from app import main

    original_provider = main.chat_api.provider
    provider = ClosableProvider()
    main.chat_api.provider = provider

    async def run_lifespan() -> None:
        async with main.lifespan(main.app):
            assert provider.closed is False

    try:
        asyncio.run(run_lifespan())
        assert provider.closed is True
    finally:
        main.chat_api.provider = original_provider