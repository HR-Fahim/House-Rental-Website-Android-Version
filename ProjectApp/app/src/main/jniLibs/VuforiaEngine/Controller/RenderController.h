/*===============================================================================
Copyright (c) 2023 PTC Inc. and/or Its Subsidiary Companies. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other
countries.
===============================================================================*/

#ifndef _VU_RENDERCONTROLLER_H_
#define _VU_RENDERCONTROLLER_H_

/**
 * \file RenderController.h
 * \brief Controller to access rendering-specific functionality in the Vuforia Engine
 */

#include <VuforiaEngine/Engine/Engine.h>
#include <VuforiaEngine/Engine/RenderConfig.h>

#ifdef __cplusplus
extern "C"
{
#endif

/** \addtogroup RenderControllerGroup Render Controller
 * \{
 * This controller allows you to control the rendering functionalities of the Vuforia SDK
 * include video background configuration.
 */

/// \brief configure the current view
typedef struct VuRenderViewConfig
{
    /// \brief Resolution of the current view or render target in pixels
    /**
     * The dimensions must consider the current orientation of the view (or UI),
     * i.e. the resolution values should differ depending on the view orientation.
     *
     * See also \ref vuPlatformControllerSetViewOrientation for more information on the view orientation.
     */
    VuVector2I resolution;
} VuRenderViewConfig;

/// \brief Video background view information
typedef struct VuVideoBackgroundViewInfo
{
    /// \brief Current viewport used for augmentation and video background rendering
    VuVector4I viewport;

    /// \brief Image size used for rendering
    VuVector2I cameraImageSize;

    /// \brief Size of the video background rendered on screen (can be stretched)
    VuVector2I vBTextureSize;
} VuVideoBackgroundViewInfo;

/// \brief Video background texture input
typedef struct VuRenderVideoBackgroundData
{
    /// \brief Render data used for video background
    /**
     * \note  OpenGL ES: NULL
     *        DX11: ID3D11Device*
     *        Metal: id<MTLRenderCommandEncoder>
     */
    const void* renderData;

    /// \brief Texture data used for video background
    /**
     * \note  OpenGL ES: NULL
     *        DX11: ID3D11Texture2D*
     *        Metal: id<MTLTexture>
     */
    const void* textureData;

    /// \brief Texture unit used for video background
    /**
     * \note  OpenGL ES: int* pointing to a variable holding the texture unit ID
     *        DX11: NULL
     *        Metal: int* pointing to a variable holding the texture unit ID
     */
    const void* textureUnitData;
} VuRenderVideoBackgroundData;

/// \brief Retrieve Render Controller to get access to rendering-specific functionality in Engine
VU_API VuResult VU_API_CALL vuEngineGetRenderController(const VuEngine* engine, VuController** controller);

/// \brief Set the current render view configuration
/**
 * \note On some devices a default, full screen, render view configuration can be determined by Engine during creation. If the device does
 * not provide the necessary information, no default render view configuration will be set and the Application must call this method
 * explicitly. You can use \ref vuRenderControllerGetRenderViewConfig after Engine creation to determine if a default render view
 * configuration was set.
 *
 * \note The default, full screen, render view configuration is setup as landscape orientation only, independent
 * of the actual interface orientation at Engine creation time. If the actual interface orientation is different from landscape at Engine
 * creation time, then \ref vuRenderControllerSetRenderViewConfig should be called immediately after Engine has been created.
 *
 * \note An updated configuration must be provided every time there are changes to the render view, e.g. when the interface rotation
 * changes.
 *
 * \returns VU_SUCCESS if the render view information was set successfully, VU_FAILED if the render view information could not be set
 */
VU_API VuResult VU_API_CALL vuRenderControllerSetRenderViewConfig(VuController* controller, const VuRenderViewConfig* renderViewConfig);

/// \brief Get the current render view configuration
/**
 * \note This call will fail if no render view config is set. See \ref vuRenderControllerSetRenderViewConfig for more details.
 *
 * \returns VU_SUCCESS on success, or VU_FAILED if no render view config is set
 */
VU_API VuResult VU_API_CALL vuRenderControllerGetRenderViewConfig(const VuController* controller, VuRenderViewConfig* renderViewConfig);

/// \brief Get the video background viewport (its location/size on screen)
/**
 * Vuforia Engine will calculate a viewport internally after Vuforia Engine has been started and a render view config has
 * been set.
 *
 * If a custom viewport has NOT been set via \ref vuRenderControllerSetVideoBackgroundViewport, Vuforia will calculate a viewport based
 * on the render view config and the current \ref VuVideoBackgroundViewportMode.
 *
 * If a custom viewport has been set via \ref vuRenderControllerSetVideoBackgroundViewport, Vuforia will calculate a viewport based on
 * the render view config and the custom viewport.
 *
 * The calculated viewport is only available if Engine is running. If no calculated viewport is available, Vuforia Engine will return
 * the viewport set via \ref vuRenderControllerSetVideoBackgroundViewport.
 *
 * If no viewport is available at all because Vuforia Engine is not running and no viewport has been set via
 * \ref vuRenderControllerSetVideoBackgroundViewport, this call will fail.
 *
 * \returns VU_SUCCESS on success, VU_FAILED if no viewport is available
 */
VU_API VuResult VU_API_CALL vuRenderControllerGetVideoBackgroundViewport(const VuController* controller, VuVector4I* vbViewport);

/// \brief Configure the video background viewport (its location/size on screen)
/**
 * \note This call will fail if no render view config is set. See \ref vuRenderControllerSetRenderViewConfig for more details.
 *
 * \note The last viewport configuration applied with either \ref vuRenderControllerSetVideoBackgroundViewport or
 * \ref vuRenderControllerSetVideoBackgroundViewportMode will override any previously set viewport
 */
VU_API VuResult VU_API_CALL vuRenderControllerSetVideoBackgroundViewport(VuController* controller, const VuVector4I* vbViewport);

/// \brief Configure the video background viewport mode
/**
 * \note This call will fail if no render view config is set. See \ref vuRenderControllerSetRenderViewConfig for more details.
 *
 * \note The video backround viewport mode may also be specified on Engine creation via the \ref VuRenderConfig. See the documentation of
 * \ref VuRenderConfig for more details.
 *
 * \note The last viewport configuration applied with either \ref vuRenderControllerSetVideoBackgroundViewport or
 * \ref vuRenderControllerSetVideoBackgroundViewportMode will override any previously set viewport
 */
VU_API VuResult VU_API_CALL vuRenderControllerSetVideoBackgroundViewportMode(VuController* controller,
                                                                             VuVideoBackgroundViewportMode vbMode);

/// \brief Get video background view information for rendering view background
/**
 * \note The video background view information is only available while Vuforia Engine is running. This call will fail if
 * Engine is not running.
 *
 * \note The video background view information will only be available when a render view config has been set. Otherwise
 * this call will fail. See \ref vuRenderControllerSetRenderViewConfig for more details.
 *
 * \warning The behaviour of this function will change in an upcoming release.
 */
VU_API VuResult VU_API_CALL vuRenderControllerGetVideoBackgroundViewInfo(const VuController* controller,
                                                                         VuVideoBackgroundViewInfo* viewInfo);

/// \brief Update the texture data to use for rendering the video background
VU_API VuResult VU_API_CALL vuRenderControllerUpdateVideoBackgroundTexture(VuController* controller, const VuState* state,
                                                                           const VuRenderVideoBackgroundData* renderVBData);

/// \brief Set the values for the near and far plane used by Engine for calculating the projection matrix
/**
 * \note These values are used in the calculation of the projection matrix reported in the \ref VuRenderState that is intended
 * for augmentation rendering (field \ref VuRenderState#projectionMatrix).
 *
 * \note Default values after Engine creation are 0.01f (near plane) and 100.0f (far plane).
 *
 * \returns VU_SUCCESS if the values have been set successfully, VU_FAILED if invalid values have been provided.
 */
VU_API VuResult VU_API_CALL vuRenderControllerSetProjectionMatrixNearFar(VuController* controller, float nearPlane, float farPlane);

/// \brief Get the values of the near and far planes currently used by Engine for calculating the projection matrix
/**
 * \returns VU_SUCCESS on success, VU_FAILED on failure to retrive the values
 */
VU_API VuResult VU_API_CALL vuRenderControllerGetProjectionMatrixNearFar(const VuController* controller, float* nearPlane, float* farPlane);

/** \} */

#ifdef __cplusplus
}
#endif

#endif // _VU_RENDERCONTROLLER_H_
