package com.github.jacokoo.kform.spring

import com.github.jacokoo.kform.KForm
import com.github.jacokoo.kform.KFormCreator
import com.github.jacokoo.kform.MapFormData
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.HandlerMapping
import java.util.concurrent.ConcurrentHashMap

class KFormArgumentResolver: HandlerMethodArgumentResolver {
    private val cache = ConcurrentHashMap<Class<*>, KFormCreator<*>>()

    @Suppress("unchecked_cast")
    override fun supportsParameter(parameter: MethodParameter) =
        KForm::class.java.isAssignableFrom(parameter.parameterType).apply {
            if (this) cache[parameter.parameterType] = KFormCreator(parameter.parameterType as Class<KForm>)
        }

    @Suppress("unchecked_cast")
    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        request: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? = (request.parameterMap + request.getAttribute(
        HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
        RequestAttributes.SCOPE_REQUEST
    ).let {
        it?.let { (it as Map<String, String>).mapValues { v -> arrayOf(v.value) } } ?: mapOf()
    })
        .filter { it.value.isNotEmpty() }
        .let { MapFormData(it.mapValues { v -> v.value[0] }) }
        .let { data -> cache[parameter.parameterType]!!.create(data) }
}