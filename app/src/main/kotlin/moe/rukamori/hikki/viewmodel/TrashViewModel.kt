/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.usecase.NoteUseCases
import moe.rukamori.hikki.domain.usecase.SettingsUseCases
import javax.inject.Inject

@HiltViewModel
class TrashViewModel
    @Inject
    constructor(
        noteUseCases: NoteUseCases,
        settingsUseCases: SettingsUseCases,
    ) : LifecycleNotesViewModel(NoteLifecycle.Trashed, noteUseCases, settingsUseCases)
