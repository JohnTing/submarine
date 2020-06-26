# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# coding: utf-8

"""
    Submarine Experiment API

    The Submarine REST API allows you to create, list, and get experiments. The API is hosted under the /v1/experiment route on the Submarine server. For example, to list experiments on a server hosted at http://localhost:8080, access http://localhost:8080/api/v1/experiment/  # noqa: E501

    The version of the OpenAPI document: 0.4.0-SNAPSHOT
    Contact: dev@submarine.apache.org
    Generated by: https://openapi-generator.tech
"""


import pprint
import re  # noqa: F401

import six

from submarine.experiment.configuration import Configuration


class JsonResponse(object):
    """NOTE: This class is auto generated by OpenAPI Generator.
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    """
    Attributes:
      openapi_types (dict): The key is attribute name
                            and the value is attribute type.
      attribute_map (dict): The key is attribute name
                            and the value is json key in definition.
    """
    openapi_types = {
        'code': 'int',
        'success': 'bool',
        'result': 'object',
        'attributes': 'dict(str, object)'
    }

    attribute_map = {
        'code': 'code',
        'success': 'success',
        'result': 'result',
        'attributes': 'attributes'
    }

    def __init__(self, code=None, success=None, result=None, attributes=None, local_vars_configuration=None):  # noqa: E501
        """JsonResponse - a model defined in OpenAPI"""  # noqa: E501
        if local_vars_configuration is None:
            local_vars_configuration = Configuration()
        self.local_vars_configuration = local_vars_configuration

        self._code = None
        self._success = None
        self._result = None
        self._attributes = None
        self.discriminator = None

        if code is not None:
            self.code = code
        if success is not None:
            self.success = success
        if result is not None:
            self.result = result
        if attributes is not None:
            self.attributes = attributes

    @property
    def code(self):
        """Gets the code of this JsonResponse.  # noqa: E501


        :return: The code of this JsonResponse.  # noqa: E501
        :rtype: int
        """
        return self._code

    @code.setter
    def code(self, code):
        """Sets the code of this JsonResponse.


        :param code: The code of this JsonResponse.  # noqa: E501
        :type: int
        """

        self._code = code

    @property
    def success(self):
        """Gets the success of this JsonResponse.  # noqa: E501


        :return: The success of this JsonResponse.  # noqa: E501
        :rtype: bool
        """
        return self._success

    @success.setter
    def success(self, success):
        """Sets the success of this JsonResponse.


        :param success: The success of this JsonResponse.  # noqa: E501
        :type: bool
        """

        self._success = success

    @property
    def result(self):
        """Gets the result of this JsonResponse.  # noqa: E501


        :return: The result of this JsonResponse.  # noqa: E501
        :rtype: object
        """
        return self._result

    @result.setter
    def result(self, result):
        """Sets the result of this JsonResponse.


        :param result: The result of this JsonResponse.  # noqa: E501
        :type: object
        """

        self._result = result

    @property
    def attributes(self):
        """Gets the attributes of this JsonResponse.  # noqa: E501


        :return: The attributes of this JsonResponse.  # noqa: E501
        :rtype: dict(str, object)
        """
        return self._attributes

    @attributes.setter
    def attributes(self, attributes):
        """Sets the attributes of this JsonResponse.


        :param attributes: The attributes of this JsonResponse.  # noqa: E501
        :type: dict(str, object)
        """

        self._attributes = attributes

    def to_dict(self):
        """Returns the model properties as a dict"""
        result = {}

        for attr, _ in six.iteritems(self.openapi_types):
            value = getattr(self, attr)
            if isinstance(value, list):
                result[attr] = list(map(
                    lambda x: x.to_dict() if hasattr(x, "to_dict") else x,
                    value
                ))
            elif hasattr(value, "to_dict"):
                result[attr] = value.to_dict()
            elif isinstance(value, dict):
                result[attr] = dict(map(
                    lambda item: (item[0], item[1].to_dict())
                    if hasattr(item[1], "to_dict") else item,
                    value.items()
                ))
            else:
                result[attr] = value

        return result

    def to_str(self):
        """Returns the string representation of the model"""
        return pprint.pformat(self.to_dict())

    def __repr__(self):
        """For `print` and `pprint`"""
        return self.to_str()

    def __eq__(self, other):
        """Returns true if both objects are equal"""
        if not isinstance(other, JsonResponse):
            return False

        return self.to_dict() == other.to_dict()

    def __ne__(self, other):
        """Returns true if both objects are not equal"""
        if not isinstance(other, JsonResponse):
            return True

        return self.to_dict() != other.to_dict()
