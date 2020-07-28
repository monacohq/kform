package com.github.jacokoo.kform

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FormProperty<T>(private val data: FormData, private val converter: Converter<T>): ReadOnlyProperty<KForm, T?> {
    override fun getValue(thisRef: KForm, property: KProperty<*>): T? =
        data[property.name].let {
            if (it.isNullOrBlank()) null
            else converter.convert(property.name, it)
        }

    fun required(): RequiredFormProperty<T> = RequiredFormProperty(data, converter)
    fun default(d: T) = DefaultValueProperty(data, converter, d)
}

class RequiredFormProperty<T>(private val data: FormData, private val converter: Converter<T>): ReadOnlyProperty<KForm, T> {
    override fun getValue(thisRef: KForm, property: KProperty<*>): T =
        data[property.name].let {
            if (it.isNullOrBlank()) throw ViolationException("${property.name} is required")
            else converter.convert(property.name, it)
        }
}

class DefaultValueProperty<T>(private val data: FormData, private val converter: Converter<T>, private val default: T): ReadOnlyProperty<KForm, T> {
    override fun getValue(thisRef: KForm, property: KProperty<*>): T =
        data[property.name]?.let { converter.convert(property.name, it) } ?: default
}