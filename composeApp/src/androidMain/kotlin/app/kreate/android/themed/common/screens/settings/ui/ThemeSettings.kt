package app.kreate.android.themed.common.screens.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kreate.android.Preferences
import app.kreate.android.R
import app.kreate.android.themed.common.component.settings.SettingComponents
import app.kreate.android.themed.common.component.settings.SettingEntrySearch
import app.kreate.android.themed.common.component.settings.SettingHeader
import app.kreate.android.themed.common.component.settings.animatedEntry
import app.kreate.android.themed.common.component.settings.entry
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.enums.ColorPaletteMode
import it.fast4x.rimusic.enums.ColorPaletteName
import it.fast4x.rimusic.ui.components.themed.ConfirmationDialog

@Composable
fun ThemeSettings( search: SettingEntrySearch ) {
    var colorPaletteName by Preferences.COLOR_PALETTE

    var resetCustomLightThemeDialog by rememberSaveable { mutableStateOf(false) }
    var resetCustomDarkThemeDialog by rememberSaveable { mutableStateOf(false) }

    if (resetCustomLightThemeDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_reset_the_custom_light_theme_colors),
            onDismiss = { resetCustomLightThemeDialog = false },
            onConfirm = {
                resetCustomLightThemeDialog = false
                Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_0.reset()
                Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_1.reset()
                Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_2.reset()
                Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_3.reset()
                Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_4.reset()
                Preferences.CUSTOM_LIGHT_TEXT.reset()
                Preferences.CUSTOM_LIGHT_TEXT_SECONDARY.reset()
                Preferences.CUSTOM_LIGHT_TEXT_DISABLED.reset()
                Preferences.CUSTOM_LIGHT_PLAY_BUTTON.reset()
                Preferences.CUSTOM_LIGHT_ACCENT.reset()
            }
        )
    }

    if (resetCustomDarkThemeDialog) {
        ConfirmationDialog(
            text = stringResource(R.string.do_you_really_want_to_reset_the_custom_dark_theme_colors),
            onDismiss = { resetCustomDarkThemeDialog = false },
            onConfirm = {
                resetCustomDarkThemeDialog = false
                Preferences.CUSTOM_DARK_THEME_BACKGROUND_0.reset()
                Preferences.CUSTOM_DARK_THEME_BACKGROUND_1.reset()
                Preferences.CUSTOM_DARK_THEME_BACKGROUND_2.reset()
                Preferences.CUSTOM_DARK_THEME_BACKGROUND_3.reset()
                Preferences.CUSTOM_DARK_THEME_BACKGROUND_4.reset()
                Preferences.CUSTOM_DARK_TEXT.reset()
                Preferences.CUSTOM_DARK_TEXT_SECONDARY.reset()
                Preferences.CUSTOM_DARK_TEXT_DISABLED.reset()
                Preferences.CUSTOM_DARK_PLAY_BUTTON.reset()
                Preferences.CUSTOM_DARK_ACCENT.reset()
            }
        )
    }

    if( search appearsIn R.string.theme  )
        SettingComponents.EnumEntry(
            Preferences.COLOR_PALETTE,
            R.string.theme
        ) {
            if( colorPaletteName == ColorPaletteName.ModernBlack )
                Preferences.THEME_MODE.value = ColorPaletteMode.System
        }

    AnimatedVisibility(visible = colorPaletteName == ColorPaletteName.CustomColor) {
        SettingComponents.ColorPicker(
            preference = Preferences.CUSTOM_COLOR,
            titleId = R.string.customcolor,
            modifier = Modifier.padding( start = 25.dp ),
            subtitle = stringResource( R.string.restarting_rimusic_is_required )
        )
    }
    AnimatedVisibility(visible = colorPaletteName == ColorPaletteName.Customized) {
        IndividualColorSection( search )
    }
}

@Composable
fun IndividualColorSection( search: SettingEntrySearch ) =
    Column {
        var resetCustomLightThemeDialog by rememberSaveable { mutableStateOf(false) }
        var resetCustomDarkThemeDialog by rememberSaveable { mutableStateOf(false) }

        if (resetCustomLightThemeDialog) {
            ConfirmationDialog(
                text = stringResource(R.string.do_you_really_want_to_reset_the_custom_light_theme_colors),
                onDismiss = { resetCustomLightThemeDialog = false },
                onConfirm = {
                    resetCustomLightThemeDialog = false
                    Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_0.reset()
                    Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_1.reset()
                    Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_2.reset()
                    Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_3.reset()
                    Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_4.reset()
                    Preferences.CUSTOM_LIGHT_TEXT.reset()
                    Preferences.CUSTOM_LIGHT_TEXT_SECONDARY.reset()
                    Preferences.CUSTOM_LIGHT_TEXT_DISABLED.reset()
                    Preferences.CUSTOM_LIGHT_PLAY_BUTTON.reset()
                    Preferences.CUSTOM_LIGHT_ACCENT.reset()
                }
            )
        }

        if (resetCustomDarkThemeDialog) {
            ConfirmationDialog(
                text = stringResource(R.string.do_you_really_want_to_reset_the_custom_dark_theme_colors),
                onDismiss = { resetCustomDarkThemeDialog = false },
                onConfirm = {
                    resetCustomDarkThemeDialog = false
                    Preferences.CUSTOM_DARK_THEME_BACKGROUND_0.reset()
                    Preferences.CUSTOM_DARK_THEME_BACKGROUND_1.reset()
                    Preferences.CUSTOM_DARK_THEME_BACKGROUND_2.reset()
                    Preferences.CUSTOM_DARK_THEME_BACKGROUND_3.reset()
                    Preferences.CUSTOM_DARK_THEME_BACKGROUND_4.reset()
                    Preferences.CUSTOM_DARK_TEXT.reset()
                    Preferences.CUSTOM_DARK_TEXT_SECONDARY.reset()
                    Preferences.CUSTOM_DARK_TEXT_DISABLED.reset()
                    Preferences.CUSTOM_DARK_PLAY_BUTTON.reset()
                    Preferences.CUSTOM_DARK_ACCENT.reset()
                }
            )
        }

        SettingHeader( R.string.title_customized_light_theme_colors )

        if( search appearsIn R.string.title_reset_customized_light_colors  )
            SettingComponents.Text(
                title = stringResource( R.string.title_reset_customized_light_colors ),
                subtitle = stringResource( R.string.info_click_to_reset_default_light_colors ),
                onClick = { resetCustomLightThemeDialog = true },
            ) {
                Icon(
                    painter = painterResource( R.drawable.trash ),
                    contentDescription = stringResource( R.string.settings_reset ),
                    tint = colorPalette().text,
                )
            }
        if( search appearsIn R.string.color_background_1  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_0,
                title = stringResource( R.string.color_background_1 )
            )
        if( search appearsIn R.string.color_background_2  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_1,
                titleId = R.string.color_background_2
            )
        if( search appearsIn R.string.color_background_3  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_2,
                titleId = R.string.color_background_3
            )
        if( search appearsIn R.string.color_background_4  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_3,
                titleId = R.string.color_background_4
            )
        if( search appearsIn R.string.color_background_5  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_THEME_BACKGROUND_4,
                titleId = R.string.color_background_5
            )
        if( search appearsIn R.string.color_text  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_TEXT,
                titleId = R.string.color_text
            )
        if( search appearsIn R.string.color_text_secondary  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_TEXT_SECONDARY,
                titleId = R.string.color_text_secondary
            )
        if( search appearsIn R.string.color_text_disabled  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_TEXT_DISABLED,
                titleId = R.string.color_text_disabled
            )
        if( search appearsIn R.string.color_icon_button_player  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_PLAY_BUTTON,
                titleId = R.string.color_icon_button_player
            )
        if( search appearsIn R.string.color_accent  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_LIGHT_ACCENT,
                titleId = R.string.color_accent
            )

        SettingHeader( R.string.title_customized_dark_theme_colors )

        if( search appearsIn R.string.title_reset_customized_dark_colors  )
            SettingComponents.Text(
                title = stringResource( R.string.title_reset_customized_dark_colors ),
                subtitle = stringResource( R.string.click_to_reset_default_dark_colors ),
                onClick = { resetCustomDarkThemeDialog = true },
            ) {
                Icon(
                    painter = painterResource( R.drawable.trash ),
                    contentDescription = stringResource( R.string.settings_reset ),
                    tint = colorPalette().text,
                )
            }
        if( search appearsIn R.string.color_background_1  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_THEME_BACKGROUND_0,
                titleId = R.string.color_background_1
            )
        if( search appearsIn R.string.color_background_2  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_THEME_BACKGROUND_1,
                titleId = R.string.color_background_2
            )
        if( search appearsIn R.string.color_background_3  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_THEME_BACKGROUND_2,
                titleId = R.string.color_background_3
            )
        if( search appearsIn R.string.color_background_4  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_THEME_BACKGROUND_3,
                titleId = R.string.color_background_4
            )
        if( search appearsIn R.string.color_background_5  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_THEME_BACKGROUND_4,
                titleId = R.string.color_background_5
            )
        if( search appearsIn R.string.color_text  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_TEXT,
                titleId = R.string.color_text
            )
        if( search appearsIn R.string.color_text_secondary  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_TEXT_SECONDARY,
                titleId = R.string.color_text_secondary
            )
        if( search appearsIn R.string.color_text_disabled  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_TEXT_DISABLED,
                titleId = R.string.color_text_disabled
            )
        if( search appearsIn R.string.color_icon_button_player  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_PLAY_BUTTON,
                titleId = R.string.color_icon_button_player
            )
        if( search appearsIn R.string.color_accent  )
            SettingComponents.ColorPicker(
                preference = Preferences.CUSTOM_DARK_ACCENT,
                titleId = R.string.color_accent
            )
    }

fun LazyListScope.themeSettingsSection( search: SettingEntrySearch ) {
    var colorPaletteName by Preferences.COLOR_PALETTE

    entry( search, R.string.theme ) {
        SettingComponents.EnumEntry(
            Preferences.COLOR_PALETTE,
            R.string.theme
        ) {
            if( colorPaletteName == ColorPaletteName.ModernBlack )
                Preferences.THEME_MODE.value = ColorPaletteMode.System
        }
    }
    animatedEntry(
        key = "colorPaletteIsNeitherPureBlackOrModernBlack",
        visible = Preferences.COLOR_PALETTE.neither( ColorPaletteName.PureBlack, ColorPaletteName.ModernBlack ),
        modifier = Modifier.padding( start = 25.dp )
    ) {
        if( search appearsIn R.string.theme_mode )
            SettingComponents.EnumEntry(
                titleId = R.string.theme_mode,
                preference = Preferences.THEME_MODE
            )
    }
    animatedEntry(
        key = "colorPaletteNameIsCustomColor",
        visible = colorPaletteName == ColorPaletteName.CustomColor,
        modifier = Modifier.padding( start = 25.dp )
    ) {
        SettingComponents.ColorPicker(
            preference = Preferences.CUSTOM_COLOR,
            titleId = R.string.customcolor,
            subtitle = stringResource( R.string.restarting_rimusic_is_required )
        )
    }
    animatedEntry(
        key = "colorPaletteNameIsCustomized",
        visible = colorPaletteName == ColorPaletteName.Customized,
        modifier = Modifier.padding( start = 25.dp )
    ) {
        IndividualColorSection( search )
    }
}