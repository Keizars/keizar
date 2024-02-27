package org.keizar.android.persistent

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val RepositoryModules = module {
    single<TokenRepository> { TokenRepository(androidContext().tokenStore) }
}