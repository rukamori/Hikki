package moe.rukamori.hikki.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.usecase.NoteUseCases
import moe.rukamori.hikki.domain.usecase.SettingsUseCases
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel
    @Inject
    constructor(
        noteUseCases: NoteUseCases,
        settingsUseCases: SettingsUseCases,
    ) : LifecycleNotesViewModel(NoteLifecycle.Archived, noteUseCases, settingsUseCases)
